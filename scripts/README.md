# Local dev scripts (Windows)

These scripts are conveniences for running OmniBot locally on Windows when MySQL,
Java, or Maven aren't installed as Windows services / aren't on your permanent PATH.

## First-time setup

Open each `.bat` file in a text editor and check the top section — if `java`, `mvn`,
or `mysqld` already work from any terminal (try `java -version`, `mvn -version`),
you can skip setting the variables and the scripts will find them automatically.

Otherwise, set these once at the top of the relevant script, or as permanent
Windows environment variables (System Properties → Environment Variables):

```bat
set JAVA_HOME=C:\path\to\your\jdk
set MAVEN_HOME=C:\path\to\your\maven
set MYSQL_HOME=C:\path\to\your\mysql
```

## Usage

| Script | What it does |
|---|---|
| `start-mysql.bat` | Starts the MySQL server in its own console window |
| `run-app.bat` | Runs `mvn spring-boot:run` for the OmniBot backend |
| `start-all.bat` | Runs both of the above in sequence |
| `deploy.bat` | All-in-one: starts MySQL (if not already running) + starts the app. Add `push` as an argument to also commit & push to GitHub first. |

### deploy.bat — the one script to rule them all

```powershell
.\scripts\deploy.bat            REM just start MySQL + app
.\scripts\deploy.bat push       REM also git add/commit/push first
```

Edit the `JAVA_HOME`, `MAVEN_HOME`, `MYSQL_HOME` lines near the top of `deploy.bat`
once to match your machine, then just double-click it (or run with `push`) every
time you want to commit your work and launch the app.


Double-click any script, or run it from a terminal:
```powershell
.\scripts\start-all.bat
```

**Tip:** Keep the MySQL console window open while developing — closing it stops
the database. The app window can be stopped with `Ctrl+C`.
