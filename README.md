# AndroidAgent 🤖

A native Android personal AI companion app powered by a local LLM. Take your AI friend with you, and watch it grow into a trusted companion that can do everything from translating text to reading and replying to your mail — with a bit of fun thrown in!

---

## ✨ Features

### 🧠 Local LLM Integration
Connect to your own local AI — no cloud subscription required. AndroidAgent supports:

| Provider | Default URL |
|----------|-------------|
| **Ollama** | `http://localhost:11434` |
| **LM Studio** | `http://localhost:1234` |
| **LLaMA.cpp Server** | `http://localhost:8080` |
| **Jan.ai** | `http://localhost:1337` |
| **GPT4All** | `http://localhost:4891` |
| **Custom OpenAI-Compatible** | *(any)* |

All configs are pre-filled with defaults — just edit the model name and hit **Fetch** to pull available models from your server.

---

### 🤖 Your Robot Team

Five named robot agents handle tasks and report back conversationally on the home screen:

| Robot | Role | Status examples |
|-------|------|-----------------|
| **ARIA 🤖** | Main AI companion — chat, mood, personality | "Ready to chat! ✨", "Thinking… 🤔" |
| **MAXIE 💬** | Messaging — SMS, WhatsApp, email monitoring & auto-reply | "New SMS from Alice! 📱", "Replying… ✍️" |
| **FELIX 📁** | File manager — workspace files, create/read/delete | "Scanning files… 📂", "File saved!" |
| **NEXUS 🌐** | Web — internet lookups and downloads | "Searching the web… 🔍" |
| **MEMO 🧠** | Memory — learns from conversations, builds knowledge | "Updating memory… 💡" |

---

### 💬 Chat Interface
- Friendly conversation bubbles (user vs ARIA messages)
- Typing indicator while the LLM responds
- Voice input button (microphone)
- Camera input button
- Per-conversation history
- Clear conversation via menu

---

### 📱 Message Monitoring & Auto-Reply
- **SMS receiver** — intercepts incoming SMS, routes to MAXIE
- **Notification listener** — monitors WhatsApp, Gmail, Outlook, and SMS apps
- **Per-contact auto-reply** — configure which contacts/chats trigger automatic LLM replies
- Per-channel toggles: SMS / Email / WhatsApp

---

### 💾 Persistent Memory (SQLite / Room)
- Every conversation is stored in an SQLite database
- **Memory table** stores key facts (name, preferences, important info) with importance scores
- Memories are injected into the LLM system context so ARIA "remembers" across sessions
- Clear individual memories or all memories from Settings

---

### 📁 File Manager
- Dedicated workspace directory on the device
- Create, read, and delete text files
- SwipeRefreshLayout to refresh
- File size and date shown in list

---

### 🎨 Animated Companion Face
`CompanionFaceView` is a custom Canvas view showing ARIA's face:
- Metallic rounded-rectangle head with gradient shading
- Blinking eyes with wandering pupils
- Mood-driven mouth (smile / talking animation)
- Antenna with glowing accent dot
- Accent colour changes with mood (blue = calm, green = happy, yellow = thinking, red = alert)

---

### ⚙️ Settings
- LLM Configuration (full CRUD with Test Connection)
- Auto-Reply master toggle + per-channel toggles
- Contacts manager for auto-reply allowlist
- Memory section (view / clear)
- App version & robot roster

---

## 🏗️ Architecture

```
app/
├── CompanionApplication.kt        # Application class; initialises DB + AgentManager
├── MainActivity.kt                # Single activity; Navigation Component + BottomNav
│
├── agents/
│   ├── AgentRobot.kt              # Data class + Robot interface + RobotMood enum
│   ├── AgentManager.kt            # Singleton; LiveData robot status; triggerRobot()
│   └── robots/
│       ├── AriaRobot.kt           # Main companion with AriaMood
│       ├── MaxieRobot.kt          # SMS / email / WhatsApp handler
│       ├── FelixRobot.kt          # File manager
│       ├── NexusRobot.kt          # Web / internet
│       └── MemoRobot.kt           # Memory & learning
│
├── data/
│   ├── database/
│   │   ├── AppDatabase.kt         # Room database (v1)
│   │   ├── dao/                   # MessageDao, MemoryDao, AgentContactDao, LLMConfigDao
│   │   └── entities/              # Message, Memory, AgentContact, LLMConfig
│   └── repository/
│       ├── ChatRepository.kt      # Wraps MessageDao + LLMManager
│       └── MemoryRepository.kt    # Memory CRUD + context-summary builder
│
├── llm/
│   ├── LLMProvider.kt             # Interface: chat(), listModels()
│   ├── LLMManager.kt              # Routes to provider; injects memory context
│   └── providers/
│       ├── OllamaProvider.kt      # Ollama streaming NDJSON
│       └── OpenAICompatibleProvider.kt  # /v1/chat/completions for all others
│
├── services/
│   ├── AgentNotificationListenerService.kt  # NotificationListenerService
│   └── AgentSmsReceiver.kt        # BroadcastReceiver for SMS_RECEIVED
│
└── ui/
    ├── main/
    │   ├── CompanionFaceView.kt   # Custom Canvas animated face
    │   └── MainFragment.kt        # Home screen: face + robot cards
    ├── chat/
    │   ├── ChatFragment.kt
    │   ├── ChatAdapter.kt         # Dual ViewHolder (user / agent bubbles)
    │   └── ChatViewModel.kt
    ├── files/
    │   ├── FilesFragment.kt
    │   ├── FilesAdapter.kt
    │   └── FilesViewModel.kt
    └── settings/
        ├── SettingsFragment.kt
        ├── LLMConfigFragment.kt   # Full LLM config with preset URLs + Fetch Models
        └── ContactsAutoReplyFragment.kt
```

**Tech stack:** Kotlin · Room (SQLite) · OkHttp · Gson · Jetpack Navigation · LiveData · Coroutines · Material Design 3 · ViewBinding

---

## 🚀 Getting Started

### Prerequisites
1. Android Studio Hedgehog (2023.1.1) or newer
2. A running local LLM server (e.g. [Ollama](https://ollama.ai/))

### Build & Run
```bash
git clone https://github.com/magicalmutation-coder/AndroidAgent.git
cd AndroidAgent
# Open in Android Studio and let Gradle sync, then Run
```

Or from the command line (requires `ANDROID_HOME` set and Gradle wrapper):
```bash
./gradlew assembleDebug
```

### First Launch
1. Grant the requested permissions (SMS, Contacts, Notifications)
2. Go to **Settings → Configure LLM Server**
3. Select your provider (e.g. Ollama), verify the URL, enter your model name, tap **Test Connection**
4. Tap **Set Active** to make it the active config
5. Return to **Home** — tap the chat bubble at the bottom to start talking to ARIA!

### Notification Listener Permission
To enable WhatsApp / email monitoring, Android requires a special system permission:

> Settings → Special app access → Notification access → AndroidAgent → Enable

The app will prompt you with instructions on first use.

---

## 📋 Permissions

| Permission | Purpose |
|-----------|---------|
| `INTERNET` | LLM server communication + web lookups |
| `READ_SMS` / `RECEIVE_SMS` / `SEND_SMS` | SMS monitoring and auto-reply |
| `READ_CONTACTS` | Contact name resolution |
| `RECORD_AUDIO` | Voice input |
| `CAMERA` | Camera input for context |
| `BIND_NOTIFICATION_LISTENER_SERVICE` | WhatsApp / email notification monitoring |
| `FOREGROUND_SERVICE` | Background processing |

---

## 🛣️ Roadmap
- [ ] Voice-to-text (SpeechRecognizer integration)
- [ ] Camera-to-text (ML Kit OCR)
- [ ] WhatsApp auto-reply via Accessibility Service
- [ ] Translation feature (NEXUS robot)
- [ ] Web search (NEXUS robot)
- [ ] Reminder / calendar integration
- [ ] Themed robot animations (walking across screen)
- [ ] Export / import conversation memories
- [ ] Multiple conversation threads

---

## 📄 License
MIT License — see [LICENSE](LICENSE) for details.
