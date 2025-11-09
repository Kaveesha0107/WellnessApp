# WellnessApp - Personal Wellness Companion

A comprehensive Android wellness application built with Kotlin and Android Studio, designed to help users manage their daily health routines and track their wellness journey.

## 🎯 Project Overview

This project was developed as part of the IT2010 – Mobile Application Development course at SLIIT. The app combines multiple wellness features to create a holistic health management solution.

## ✨ Features

### 🔐 Security & Onboarding
- **Onboarding Screen**: Interactive introduction to app features
- **PIN Security**: 4-digit PIN protection for data privacy
- **Professional UI**: Modern Material Design 3 interface

### 💪 Daily Habit Tracker
- Add, edit, and delete daily wellness habits
- Visual progress tracking with progress bars
- Emoji selection for habit customization
- Step counter integration using device sensors
- Share progress functionality

### 😊 Mood Journal
- Emoji-based mood logging with 10 different mood options
- Optional notes for each mood entry
- Mood trend visualization using MPAndroidChart
- Shake-to-add quick mood entry feature
- Calendar view of past moods
- Share mood summary functionality

### 💧 Hydration Reminder
- Smart water intake tracking
- Configurable reminder intervals (30 min - 4 hours)
- WorkManager-based background notifications
- Daily water count reset
- Visual water intake display

### 📱 Home Screen Widget
- Real-time habit completion percentage
- Quick access to app from home screen
- Professional widget design
- Automatic updates

### 🔧 Advanced Features
- **Sensor Integration**: Accelerometer for shake detection, step counter for activity tracking
- **Data Persistence**: SharedPreferences for storing user data
- **Responsive UI**: Adapts to different screen sizes and orientations
- **Navigation**: Bottom navigation with 4 main sections
- **Data Management**: Clear all data functionality

## 🏗️ Technical Architecture

### Architecture Pattern
- **Fragment-based Architecture**: MainActivity with multiple fragments
- **Navigation Component**: Jetpack Navigation for fragment management
- **MVVM Pattern**: Separation of concerns with proper data management

### Key Components
- **Activities**: OnboardingActivity, SecurityActivity, DashboardActivity, MainActivity
- **Fragments**: DashboardFragment, HabitsFragment, MoodJournalFragment, SettingsFragment
- **Adapters**: HabitAdapter, MoodAdapter, OnboardingAdapter
- **Utils**: SharedPrefsManager, NotificationHelper
- **Workers**: HydrationReminderWorker for background tasks
- **Widgets**: HabitWidgetProvider for home screen widget

### Data Management
- **SharedPreferences**: For storing user settings, habits, mood entries, and security PIN
- **Gson**: For JSON serialization/deserialization
- **No Database**: As per requirements, uses SharedPreferences instead of SQLite

### Dependencies
- **AndroidX Libraries**: Core, AppCompat, Material Design, Navigation
- **WorkManager**: For background task scheduling
- **MPAndroidChart**: For mood trend visualization
- **Gson**: For data serialization

## 📱 Screenshots

The app features a modern, professional interface with:
- Clean card-based layouts
- Consistent color scheme
- Intuitive navigation
- Responsive design for different screen sizes

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 24+ (Android 7.0)
- Kotlin 1.8+

### Installation
1. Clone the repository
2. Open in Android Studio
3. Sync Gradle files
4. Run on device or emulator

### First Run
1. Complete the onboarding process
2. Set up your 4-digit security PIN
3. Start adding habits and logging moods
4. Configure hydration reminders
5. Add the widget to your home screen

## 🔧 Configuration

### Permissions
The app requires the following permissions:
- `POST_NOTIFICATIONS`: For hydration reminders
- `VIBRATE`: For notification feedback
- `ACTIVITY_RECOGNITION`: For step counting
- `WAKE_LOCK`: For background processing

### Settings
- **Hydration Reminders**: Enable/disable and set interval
- **Data Management**: Clear all app data
- **Security**: Change PIN (future enhancement)

## 📊 Data Storage

All user data is stored locally using SharedPreferences:
- **Habits**: List of daily habits with progress tracking
- **Mood Entries**: Timestamped mood logs with notes
- **Settings**: Hydration preferences and app configuration
- **Security**: Encrypted PIN storage

## 🎨 UI/UX Design

### Design Principles
- **Material Design 3**: Following Google's latest design guidelines
- **Accessibility**: Proper contrast ratios and touch targets
- **Consistency**: Unified color scheme and typography
- **Responsiveness**: Adapts to different screen sizes

### Color Scheme
- **Primary**: Green (#00C853)
- **Secondary**: Amber (#FFC107)
- **Background**: Light Gray (#F5F5F5)
- **Surface**: White (#FFFFFF)
- **Error**: Red (#F44336)

## 🔒 Security Features

- **PIN Protection**: 4-digit PIN required for app access
- **Data Privacy**: All data stored locally on device
- **Secure Storage**: PIN stored using Android's secure storage
- **No Network**: No data transmission to external servers

## 📈 Performance Optimizations

- **Efficient RecyclerViews**: Proper ViewHolder pattern
- **Background Processing**: WorkManager for notifications
- **Memory Management**: Proper lifecycle handling
- **Sensor Optimization**: Efficient sensor usage

## 🧪 Testing

The app includes:
- **Unit Tests**: Basic functionality testing
- **Instrumented Tests**: UI and integration testing
- **Manual Testing**: Comprehensive feature testing

## 📝 Future Enhancements

Potential improvements for future versions:
- **Cloud Sync**: Backup data to cloud storage
- **Advanced Analytics**: Detailed wellness insights
- **Social Features**: Share progress with friends
- **Custom Themes**: Dark mode and color customization
- **Export Data**: CSV/PDF export functionality
- **Reminder Customization**: More notification options

## 🤝 Contributing

This is an academic project, but suggestions and improvements are welcome:
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## 📄 License

This project is developed for educational purposes as part of the IT2010 course at SLIIT.

## 👨‍💻 Developer

Developed by [Your Name] for IT2010 – Mobile Application Development, SLIIT 2025.

## 📞 Support

For questions or issues related to this project, please contact the developer or refer to the course documentation.

---

**Note**: This app is designed for educational purposes and demonstrates various Android development concepts including fragments, navigation, data persistence, sensors, notifications, and widgets.
