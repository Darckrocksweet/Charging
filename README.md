# Remix Зарядка EV (EV Charge Tracker)

Android-приложение для раздельного учета зарядок электромобиля (со стороны станции и со стороны авто), расчета потерь энергии, статистики и экспорта данных.

## Автоматическая сборка и публикация в GitHub Actions

В проекте настроен GitHub Actions workflow: `.github/workflows/release.yml`.

### Возможности:
1. **Автоматическая сборка APK при каждом push и PR**:
   - При пуше в ветки `main` или `master` собирается отладочный APK (`assembleDebug`).
   - Собранный APK загружается в артефакты сборки (**Artifacts**) на странице выполненного workflow (доступен для скачивания 14 дней).

2. **Автоматическая публикация GitHub Release по тегу**:
   - Создайте и отправьте тег версии:
     ```bash
     git tag v1.0.0
     git push origin v1.0.0
     ```
   - GitHub Actions автоматически соберет `app-debug.apk` и создаст полноценный GitHub Release с прикрепленным APK-файлом и списком изменений.

3. **Ручной запуск из интерфейса GitHub (Workflow Dispatch)**:
   - Перейдите во вкладку **Actions** в репозитории на GitHub.
   - Выберите workflow **Build & Release Debug APK**.
   - Нажмите **Run workflow** (можно указать произвольный тег и название релиза, либо оставить пустыми для автоматической нумерации).

## Локальная сборка

```bash
./gradlew assembleDebug
```
Готовый APK будет находиться по пути:
`app/build/outputs/apk/debug/app-debug.apk`
