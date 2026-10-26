# Промпт для бэкенда — удаление аккаунта (DebtTrackerAPI)

---

Добавь в существующий REST API DebtTrackerAPI (FastAPI + SQLAlchemy + SQLite, эндпоинты `/api/auth/*`, `/api/debts/*`, `/api/payments`) эндпоинт, с помощью которого пользователь удаляет **собственный** аккаунт вместе со всеми своими данными. Мобильное приложение «Раздам долги» уже вызывает его из меню «⋮ → Удалить аккаунт»; магазин приложений (RuStore) требует, чтобы пользователь мог удалить созданный аккаунт.

Следуй правилам из `AGENTS.md` репозитория: роутер тонкий, логика в `app/crud/`, ответы об ошибках в формате `{"detail": "..."}`, перед коммитом `python -m ruff check . && python -m ruff format --check .` без ошибок.

---

### 1. Контракт

| Параметр | Значение |
|---|---|
| Метод и путь | `DELETE /api/auth/me` (под тем же `<API_PATH_PREFIX>`, что и остальные маршруты) |
| Авторизация | `Authorization: Bearer <accessToken>` (как у `/api/auth/logout`) |
| Тело запроса | нет |
| Успех | `204 No Content`, пустое тело |
| Ошибки | `401` — токен отсутствует, просрочен или пользователь уже удалён (тот же ответ, что выдаёт `get_current_user`); `429` — превышен лимит |
| Лимит | `5/minute` на IP (`@limiter.limit("5/minute")`, как у `login`/`register`) |

Приложение отправляет ровно `DELETE {base}auth/me` с заголовком `Authorization`. Если в ответ придёт `401`, оно один раз попробует обновить токен и затем разлогинит пользователя — менять ничего не нужно.

---

### 2. Поведение (обязательно)

1. Удалить пользователя (`User`) и **каскадно** все его данные в **одной транзакции**: долги (`Debt`), их платежи (`Payment`) и refresh-токены (`RefreshToken`). Каскады уже описаны в `app/models.py` (`cascade="all, delete-orphan"`), достаточно `db.delete(user)` + `db.commit()`.
2. Работает одинаково для обычных аккаунтов (с паролем) и для аккаунтов VK ID (`passwordHash IS NULL`, заполнен `vkId`).
3. После удаления:
   - старый `accessToken` перестаёт работать сразу — `get_current_user` вернёт `401 "User not found"`, ничего дополнительно делать не нужно;
   - `refresh` по старому refresh-токену возвращает `401`, `login` по старым логину и паролю — `401`;
   - `username` освобождается и может быть зарегистрирован снова;
   - повторный вызов `DELETE /api/auth/me` тем же токеном — `401`.
4. Данные других пользователей не затрагиваются.
5. Аккаунт ВКонтакте не удаляется и не изменяется — сервер ничего не вызывает в VK.
6. Логирование: на уровне INFO писать только числовой `id` пользователя (`"Account deleted: id=%d"`), без логина, имени и VK ID.
7. Миграция БД **не нужна** — схема не меняется.

---

### 3. Реализация (по структуре проекта)

Новый файл `app/crud/users.py`:

```python
import logging

from sqlalchemy.orm import Session

from app.models import User

logger = logging.getLogger(__name__)


def delete_user(db: Session, user: User) -> None:
    user_id = user.id
    db.delete(user)
    db.commit()
    logger.info("Account deleted: id=%d", user_id)
```

В `app/routers/auth.py` (импорт `from app.crud import users as users_crud`):

```python
@router.delete("/me", status_code=204)
@limiter.limit("5/minute")
def delete_account(
    request: Request,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    users_crud.delete_user(db, current_user)
    return None
```

`get_db` и `get_current_user` используют одну сессию на запрос, поэтому `current_user` можно удалять напрямую.

---

### 4. Тесты (в `tests/test_auth.py`)

Обязательно покрыть:

1. **Удаляются все данные пользователя:** зарегистрироваться, создать долг и платёж, вызвать `DELETE /api/auth/me` → `204`; в БД нет ни `User`, ни `Debt`, ни `Payment`, ни `RefreshToken`.
2. **Учётные данные перестают работать:** после удаления `GET /api/debts` со старым токеном → `401`; `refresh` со старым refresh-токеном → `401`; `login` → `401`.
3. **Без токена:** `DELETE /api/auth/me` → `401`.
4. **Изоляция:** при удалении пользователя A данные пользователя B остаются нетронутыми.
5. **`username` освобождается:** после удаления повторная регистрация того же имени → `201`.
6. **VK-аккаунт:** создать `User(username="vk_123", vkId="123", passwordHash=None)` напрямую в БД, выпустить токен через `create_access_token(user.id)`, удалить → `204`, пользователя в БД нет.

---

### 5. Документация и учёт изменений

- `AGENTS.md`: строка в таблице эндпоинтов (`DELETE /api/auth/me`), файл `app/crud/users.py` в структуре проекта, шаг 5 в блоке Auth Flow, актуальное число тестов.
- `README.md`: пункт в Features, число тестов, лимит 5 req/min в строке про rate limiting.
- `CHANGELOG.md`: запись «Account deletion».
- В Bruno-коллекцию запрос добавлять только в самый конец dev-прогона: удаление аккаунта сломает все последующие запросы, которые используют того же пользователя.

---

### 6. Выкладка и проверка

Схема БД не меняется, достаточно пересобрать и перезапустить контейнер:

```bash
docker compose --profile proxy up -d --build
```

Проверка на реальном сервере (подставьте свои `<server>` и `<API_PATH_PREFIX>`):

```bash
BASE=https://<server>:8443/<API_PATH_PREFIX>/api
TOKEN=$(curl -sk -X POST $BASE/auth/register -H 'Content-Type: application/json' \
  -d '{"username":"delete_check","password":"secret123"}' | python -c 'import sys,json;print(json.load(sys.stdin)["accessToken"])')

curl -sk -o /dev/null -w "delete: %{http_code}\n" -X DELETE $BASE/auth/me -H "Authorization: Bearer $TOKEN"   # ожидается 204
curl -sk -o /dev/null -w "after:  %{http_code}\n" $BASE/debts -H "Authorization: Bearer $TOKEN"              # ожидается 401
```

---

### 7. Что удаление не затрагивает

Эндпоинт удаляет только данные в рабочей базе. Если у сервера есть резервные копии БД или журналы, в которых остаются данные пользователя (например, логин при неудачной попытке входа попадает в журнал ошибок), они этим эндпоинтом не очищаются. Политика конфиденциальности приложения обещает удаление «сразу после подтверждения» — если у вас есть бэкапы, сообщите об этом владельцу приложения, чтобы формулировка в политике была точной.
