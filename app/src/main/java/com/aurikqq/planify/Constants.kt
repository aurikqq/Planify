package com.aurikqq.planify

val mainScreenTabs = arrayOf(PLANS_SCREEN, NOTES_SCREEN)

const val PREFERENCES_NAME = "com.aurikqq.planify.AppPreferences"
const val KEY_PLANS = "user_plans"
const val KEY_DAILY_PLANS_HISTORY = "daily_plans_history"
const val KEY_NOTES_LIST = "notes_list"
const val KEY_DAILY_PLANS_LIST = "daily_plans_list"

const val KEY_IS_UPDATE_CHANGELOG_SHOWN = "is_update_changelog_shown"

const val KEY_TEMP_PLANS = "temp_plans"
const val KEY_TEMP_NOTE_TITLE = "temp_note_title"
const val KEY_TEMP_NOTE_TEXT = "temp_note_text"

const val KEY_HAVE_PLANS = "does_user_have_plans"
const val KEY_IS_FIRST_LAUNCH = "is_first_launch"

const val MAIN_SCREEN = "main"
const val PLANS_SCREEN = "plans"
const val NOTES_SCREEN = "notes"
const val HISTORY_SCREEN = "history"
const val SETTINGS_SCREEN = "settings"

const val PLANS_NOTIFICATIONS_ENABLED = "plans_notifications_enabled"
const val RESET_NOTIFICATIONS_ENABLED = "notes_notifications_enabled"
const val PLANS_NOTIFICATIONS_COOLDOWN = "plans_notifications_cooldown"
const val IS_DARK_THEME_ON = "is_dark_theme_on"
const val USER_EMAIL = "user_email"
const val CHANGELOG = "Версия 0.3.2:\n" +
        "• В Планифай пришло Рождество и всякое такое, так что теперь тут сыпет снег, ну и всяких декораций ещё нарисовал (ага, сам! знаю, мне лучше больше не браться за это!), короче it's snowing\n\n" +
        "• Теперь надписи в меню планов чередуются: добавлены новые. Скоро так будет со всеми. Надписями.\n\n" +
        "• Починено следующее:\n" +
        "    - Проверка обновлений иногда не срабатывала, в зависимости от номеров версий\n" +
        "    - Пара проблем с анимациями присутствовала\n" +
        "    - А ещё обновления не устанавливались, вот прям вообще ни в какую(\n\n\n" +
        "Версия 0.3.1:\n" +
        "• Первое обновление интерфейса приложения (да, оно будет не одно)\n" +
        "    - История переместилась на отдельный экран, открывающийся иконкой в верхней панели.\n" +
        "    - Новый дизайн нижней панели.\n" +
        "    - Изменён вид экрана дневных планов; теперь планы и записи оформлены в едином стиле.\n" +
        "    - Испепелено и стёрто с лица Земли множество проблем с анимацией (например, кнопки в записях скрывались во время раскрытия текста), конфликтов интерфейса (список дней был за задней панелью), багов и прочих непристойностей.\n" +
        "    - И много других улучшений, анимаций и прочего приятного глазу.\n\n" +
        "• Добавлены настройки: отлючение отправки уведомлений и её интервал, смена темы приложения, список изменений и красивый дизайн. Будут добавляться и другие параметры!\n\n" +
        "• В очередной раз исправлена работа уведомлений... \uD83D\uDE36\u200D\uD83C\uDF2B\uFE0F\n\n" +
        "• Добавлена возможность отправить логи для того, чтобы я мог понять источник ошибки - даже если Планифай не запускается.\n\n" +
        "• ❄\uFE0F\uD83E\uDD5A\n\n"
