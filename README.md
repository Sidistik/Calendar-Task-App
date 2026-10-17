# Calendar Task App

An Android calendar and task management application developed using **Kotlin** and **Android Studio**. The app helps users organise tasks by due date and track their completion through a calendar interface.

## Features

- **User registration and login** to access personal tasks.
- **Monthly calendar view** with date selection and month navigation.
- **Task management:** create, edit, and delete tasks associated with a date.
- **Task details:** title, description, due date, and priority.
- **Completion tracking:** mark tasks as completed or incomplete.
- **Local storage:** save user and task records using Room Database.
- **Saved sessions:** maintain the current user's session locally.

## Technologies Used

- **Kotlin** — application logic
- **Android Studio** — development environment
- **Jetpack Compose / Material 3** — user interface
- **Room Database** — local persistence
- **Navigation Compose** — navigation between screens
- **ViewModel and Kotlin Flow** — UI state management
- **Gradle** — project build configuration

## Project Structure

```text
app/src/main/java/
├── data/
│   ├── local/          # Database, entities, and DAOs
│   └── repository/     # Authentication and task data access
├── ui/
│   ├── navigation/     # App navigation
│   ├── screens/        # Login, registration, calendar, and task editing
│   └── theme/          # UI theme
├── viewmodel/          # Authentication and task state
└── MainActivity.kt     # Application entry point
```

## Running the Project

1. Clone or download this repository.
2. Open the project folder in **Android Studio**.
3. Allow Gradle to sync and install the required dependencies.
4. Select an Android emulator or connect an Android device.
5. Click **Run** to launch the application.

## Notes

This is a learning project demonstrating Android app development, local data persistence, and calendar-based task organisation. Authentication uses a local user database; it is **not intended as production-ready authentication**. In particular, the supplied implementation stores user passwords without secure password hashing. Do not use real passwords when testing.

## Author

Developed as a university programming project.
