<div align="center">

# 📄 CV Insight

**An AI-powered desktop app that scores your CV, tells you how to fix it, and shows you how the pros do it.**

Upload a resume, get an instant 0–100 score with concrete feedback, build a polished CV from scratch with a live preview, and compare yourself side-by-side against high-scoring example CVs from Google, Apple, Netflix, Figma, and MIT — all in a native desktop app powered by a **local** AI model, so your CV never leaves your machine.

![Java](https://img.shields.io/badge/Java-21-orange)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue)
![Maven](https://img.shields.io/badge/Build-Maven-C71A36)
![SQLite](https://img.shields.io/badge/Database-SQLite-003B57)
![Ollama](https://img.shields.io/badge/AI-Ollama%20%C2%B7%20llama3-black)
![License](https://img.shields.io/badge/License-MIT-green)

</div>

---

## 📖 About The Project

**CV Insight** is a JavaFX desktop application that brings resume analysis, resume building, and resume benchmarking into one tool. It runs entirely on your computer and uses a **local Ollama LLM** for AI feedback — there are no external API calls and no resume data sent to the cloud.

Whether you're a student applying for your first internship, a professional polishing your CV before a job switch, or just curious how your resume stacks up against people who landed roles at top companies, CV Insight gives you:

- 🧮 An objective **AI score out of 100** with strengths, weaknesses, and actionable suggestions
- 🏗️ A **live resume builder** with instant HTML preview and PDF export
- ⚖️ A **side-by-side comparison** against curated example CVs from top companies
- 📊 A personal **dashboard** tracking every resume, score, and trend over time

Everything is stored locally in a SQLite database, so your work persists between sessions and stays private.

---

## ✨ Features

| Feature | Description |
| --- | --- |
| 🔐 **User Accounts** | Register and log in securely. Passwords are hashed with BCrypt — never stored in plain text. |
| 📤 **Upload & Parse** | Drop in a PDF or TXT resume. Text is extracted and automatically split into sections (Summary, Experience, Education, Skills, Projects). |
| 🤖 **AI Analysis** | A local LLM scores your CV 0–100 and returns specific strengths, weaknesses, and improvement suggestions. |
| 🏗️ **Resume Builder** | Fill in personal info, education, experience, projects, and skills with a real-time HTML preview. |
| 🖨️ **PDF Export** | Print or export your built resume straight to PDF. |
| ⚖️ **Compare with Examples** | Benchmark your CV against bundled top-company examples using a Technical or General scoring strategy, plus an AI side-by-side breakdown. |
| 🔎 **Searchable Examples** | Filter example CVs by company, role, or category. |
| 📊 **Dashboard & Stats** | See total resumes, average score, best score, and personalized AI tips at a glance. |
| 🗂️ **Persistent History** | Every resume and analysis is saved to a local SQLite database and survives restarts. |
| 🔒 **100% Local & Private** | AI runs on your machine via Ollama. Your resume never leaves your computer. |

---

## 🛠️ Tech Stack

- **Java 21** — core language (uses virtual threads for async analysis)
- **JavaFX 21** — desktop UI framework (FXML + CSS styling)
- **Maven** — build, dependency management, and packaging
- **SQLite** (`xerial sqlite-jdbc`) — local persistence
- **Apache PDFBox 3** — PDF text extraction and rendering
- **OkHttp** — HTTP client for talking to the local AI server
- **Gson** — JSON parsing of AI responses
- **jBCrypt** — secure password hashing
- **Ollama + llama3** — local large language model serving the AI analysis
- **JUnit 5** — testing

**Design patterns showcased:** Singleton · Factory Method · Strategy · Observer · Facade

---

## 🚀 Getting Started

Follow these steps to get a local copy up and running.

### Prerequisites

You'll need the following installed:

- **JDK 21 or higher** — verify with `java -version`
- **Apache Maven** — verify with `mvn -version`
- **[Ollama](https://ollama.com)** running locally with the `llama3` model pulled — this powers the AI analysis
- **Git** for cloning the repository

Verify your installations:

```bash
java -version     # should print 21.x or higher
mvn -version      # should print Apache Maven 3.x
git --version
```

Set up the local AI model:

```bash
ollama pull llama3     # download the model (~4.7 GB, one time)
ollama serve           # start the server (often runs automatically after install)
```

The app expects Ollama at `http://localhost:11434`. Make sure it's running before you analyze a CV.

### Installation

**1. Clone the repository**

```bash
git clone <your-repo-url>
```

**2. Navigate into the project folder**

```bash
cd cv-insight
```

**3. Build and download dependencies**

```bash
mvn clean install
```

Maven pulls everything in `pom.xml` (JavaFX, PDFBox, OkHttp, Gson, jBCrypt, SQLite driver, and test tools) automatically.

### Running the App

Start the application:

```bash
mvn javafx:run
```

On **Windows**, you can alternatively use the included script:

```bat
run.bat
```

On first launch the app creates a local SQLite database (`cvinsight.db`) and seeds the example CVs automatically. Register an account, log in, and you're ready to go.

> 💡 Make sure **Ollama is running** before triggering an analysis, or the AI step will fail with a network error.

---

## 📦 Building for Production

To package the app into a single runnable fat-jar:

```bash
mvn clean package
```

The output lands at:

```
target/cv-insight.jar
```

The Maven Shade plugin bundles all dependencies into that one jar. Run it with:

```bash
java -jar target/cv-insight.jar
```

> ⚠️ If you see **"JavaFX runtime components are missing"**, your environment isn't supplying the JavaFX modules. Prefer `mvn javafx:run`, or pass the JavaFX module path explicitly:
> ```bash
> java --module-path /path/to/javafx-sdk/lib \
>      --add-modules javafx.controls,javafx.fxml \
>      -jar target/cv-insight.jar
> ```
> See `.vscode/launch.json` for a working VM-args example.

---

## 🌐 Distribution

CV Insight is a desktop application, not a website — so "deployment" means shipping a runnable artifact to users rather than hosting a server.

| Method | How |
| --- | --- |
| **Fat JAR** | Run `mvn clean package` and share `target/cv-insight.jar`. Users need JDK 21 to run it. |
| **Native installer** | Use [`jpackage`](https://docs.oracle.com/en/java/javase/21/jpackage/) (bundled with the JDK) to produce a platform-native `.exe`, `.dmg`, or `.deb` with an embedded runtime — no JDK required on the user's machine. |
| **From source** | Users clone the repo and run `mvn javafx:run`. |

> Whichever method you choose, **end users still need Ollama installed and running** for the AI features to work. Document this in your release notes.

---

## 📁 Project Structure

```
cv-insight/
├── pom.xml                          # Maven build & dependencies
├── run.bat                          # Windows launch script
├── src/
│   ├── main/java/com/cvinsight/
│   │   ├── app/                     # MainApp — application entry point
│   │   ├── ai/                      # AI pipeline (Facade pattern)
│   │   │   ├── client/              #   → HTTP client to local LLM
│   │   │   ├── prompt/              #   → builds the analysis prompt
│   │   │   ├── parser/              #   → parses AI JSON into models
│   │   │   └── facade/              #   → one-call analysis entry point
│   │   ├── comparison/              # Scoring strategies (Strategy pattern)
│   │   ├── cv/parser/               # PDF & TXT parsers (Factory pattern)
│   │   ├── db/                      # DatabaseManager (Singleton), DAOs, seeder
│   │   ├── model/                   # CV, User, Score, Feedback, sections...
│   │   ├── service/                 # Business logic & async analysis (Observer)
│   │   └── ui/                      # SceneManager, controllers, components
│   └── main/resources/
│       ├── com/cvinsight/ui/        # FXML views + styles.css
│       └── config.properties        # Model configuration
└── README.md                        # You're reading it!
```

---

## 🏛️ Architecture

CV Insight deliberately demonstrates five classic design patterns — useful if you're studying software design:

| Pattern | Where | Why |
| --- | --- | --- |
| **Singleton** | `DatabaseManager`, `SessionManager` | One shared DB connection and one current-user session for the whole app. |
| **Factory Method** | `CVParserFactory` | Picks the right parser (PDF vs TXT) by file extension; adding a format means adding one class. |
| **Strategy** | `ScoringStrategy` → `Technical` / `General` | Swap the CV comparison algorithm at runtime without touching the engine. |
| **Observer** | `AnalysisService` → `AnalysisController` | The async AI pipeline pushes live progress updates to the UI; neither side is coupled to the other. |
| **Facade** | `AIAnalysisFacade` | Hides the prompt → API → parse pipeline behind a single `analyze(cv)` call. |

The AI analysis runs on a **Java 21 virtual thread**, so the UI stays responsive while the model works, with progress events streamed back to the screen.

---

## 🎮 Usage

Once the app is running and you're logged in:

| Action | What it does |
| --- | --- |
| **Upload Resume** | Pick a PDF/TXT → get an AI score, strengths, weaknesses, and suggestions. |
| **Build Resume** | Create a CV from scratch with a live preview, then export to PDF. |
| **Browse Examples** | Explore curated top-company CVs and see what high scores look like. |
| **Compare** | After analyzing your CV, run a side-by-side AI comparison against any example. |
| **Re-analyze** | Re-run analysis on any saved resume from the dashboard. |

**Supported file types:** `.pdf` (text-based, not scanned) and `.txt`.

---

## ⚙️ Configuration

Model settings live in `src/main/resources/config.properties`:

```properties
anthropic.api.key=YOUR_API_KEY_HERE
claude.model=claude-sonnet-4-6
claude.max_tokens=1024
```

> ⚠️ **Heads up:** The active AI client (`ai/client/ClaudeApiClient.java`) currently calls a **local Ollama server** (`http://localhost:11434`) using the `llama3` model. The Claude/Anthropic values above are not yet wired into that client. To change the model or endpoint, edit the `API_URL` and `MODEL` constants in `ClaudeApiClient.java`.

---

## 🧪 Running Tests

```bash
mvn test
```

Tests use JUnit 5 (Jupiter).

---

## 🐛 Troubleshooting

| Problem | Fix |
| --- | --- |
| Analysis fails with a network error | Ensure Ollama is running (`ollama serve`) and `llama3` is pulled. |
| "JavaFX runtime components are missing" | Use `mvn javafx:run`, or pass the JavaFX module path when running the jar. |
| PDF "appears to be empty or image-only" | The PDF is scanned/image-based. Use a text-based PDF or a `.txt` file. |
| Database errors on startup | Delete `cvinsight.db` to let the app recreate and reseed it. |
| Analysis is very slow | Local LLM responses depend on your hardware. The client allows up to 120s before timing out. |

---

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📜 License

Distributed under the MIT License. See `LICENSE` for more information.

---

## 🙏 Acknowledgments

- [Ollama](https://ollama.com) — local LLM serving
- [Apache PDFBox](https://pdfbox.apache.org) — PDF parsing and rendering
- [OpenJFX](https://openjfx.io) — the JavaFX platform
- [jBCrypt](https://www.mindrot.org/projects/jBCrypt/) — password hashing
- [SQLite JDBC](https://github.com/xerial/sqlite-jdbc) — embedded database driver

---

## 📬 Contact

**Emrecan Bektas** — [@emrecanbektas](https://github.com/emrecanbektas)
**Baran Salis** — [@Baransalis42](https://github.com/Baransalis42)
**Omer Faruk Coskun** — [@omarfarukcoskun(https://github.com/omarfarukcoskun)


<div align="center">

**CV Insight** — build stronger resumes.

⭐ If you find this project useful, consider giving it a star!

</div>
