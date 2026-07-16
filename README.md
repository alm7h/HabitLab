# HabitLab — Offline-First Android-App für Habit-Tracking

**HabitLab** ist eine native, vollständig offline-fähige Android-App zur komfortablen Nachverfolgung täglicher Gewohnheiten (Habits). Das Projekt wurde im Studiengang *Angewandte Informatik* im Rahmen des Moduls *Mobile Betriebssysteme und Netzwerke* entwickelt. 

Die App basiert auf modernen Android-Entwicklungsstandards wie **Kotlin**, **Jetpack Compose** und folgt einer sauberen **MVVM-Architektur** in Kombination mit dem **Repository-Pattern**.

---

## 🚀 Kernfunktionen

*   **Gewohnheiten verwalten (Habit Management):** Erstellen, Bearbeiten, Archivieren und Löschen von Gewohnheiten mit individuellem Namen, Icon-Keys und Hex-Farbcodes.
*   **Habit-Abschluss:** Markieren von Gewohnheiten als erledigt direkt am aktuellen Tag mit einfacher Option zum Rückgängigmachen (maximal ein Eintrag pro Habit und Tag).
*   **Intelligentes Streak-System:** Automatische Berechnung und Persistierung der aktuellen sowie der historischen Best-Streaks (Aktivitätsserien in Tagen) nach jedem aufgezeichneten Eintrag.
*   **Statistik-Dashboard:** Aufbereitung von Produktivitätsdaten, monatlicher Auswertung je Habit, wöchentlicher Fortschrittsquote sowie Gesamtstatistiken.
*   **Aktivitäts-Heatmap:** GitHub-ähnliche, visuelle Darstellung der täglichen Abschlussquote im Zeitverlauf zur Motivation langfristiger Konsistenz.
*   **Offline-First:** Volle Funktionsfähigkeit komplett ohne Internetverbindung durch lokale Datenhaltung mit SQLite/Room-Datenbank.

---

## 🛠️ Architektur & Datenfluss

Die App implementiert das **Model-View-ViewModel (MVVM)** Entwurfsmuster gepaart mit dem **Repository Pattern** zur sauberen Kapselung der Datenquellen. Bei der Entwicklung wurde nach dem **API-First-Prinzip** gearbeitet, wodurch alle Kernkomponenten zunächst über strikte Interfaces definiert wurden.

### Schichtenübersicht

*   **View (UI):** Jetpack Compose Screens (Dashboard, Habits, Statistics) – Reine Darstellung und Weiterleitung von Benutzerinteraktionen.
*   **ViewModel:** Verwaltung des UI-State (`HabitVM`, `StatsVM`, `DashboardVM`) und Kapselung der Use-Case-Logik.
*   **Repository:** Abstraktionsschicht (`HabitRepo`, `EntryRepo`, `StreakRepo`) zur Entkopplung der Business-Logik von der konkreten Persistenzdatenquelle.
*   **Model / DB:** Lokale Room-Entitäten und DAOs (`HabitDao`, `EntryDao`, `StreakDao`) als Schnittstelle zur SQLite-Datenbank.

### Reaktiver Datenfluss

```text
User Event ➔ ViewModel ➔ Repository ➔ Room DB DAO ➔ LiveData/StateFlow ➔ ViewModel ➔ UI-State-Update
```

---

## 📊 Datenbankmodell (Room DB)

Die lokale Datenbank (`AppDatabase`) besteht aus drei relationalen Tabellen. Zur Gewährleistung der referentiellen Integrität sind Kaskadierungen (`ON DELETE CASCADE`) implementiert:

```
    [ habit ]  1 ─────────── N  [ entry ] (Max. 1 Eintrag pro Habit & Tag)
        │
        │ 1
        │
        ▼
    [ streak ] (Persistierter Streak-Zustand, 1:1 zu habit)
```

### 1. Tabelle: `habit`
Speichert die vom Nutzer definierten Gewohnheiten.
*   `id` (Long, PK, AutoGenerate): Eindeutiger Primärschlüssel.
*   `name` (String, NOT NULL): Anzeigename der Gewohnheit.
*   `iconKey` (String, NOT NULL): Schlüsselwert für das Icon.
*   `colorHex` (String, NOT NULL): Farbwahl im Hex-Format.
*   `isArchived` (Boolean, NOT NULL, Default: false): Archivierungsstatus.
*   `createdAt` (Long, NOT NULL): Zeitstempel der Erstellung.

### 2. Tabelle: `entry`
Speichert einen Datensatz pro abgeschlossenem Tag je Gewohnheit.
*   `id` (Long, PK, AutoGenerate): Eindeutiger Primärschlüssel.
*   `habitId` (Long, FK): Verweis auf `habit(id)` mit `ON DELETE CASCADE`.
*   `date` (Long, NOT NULL): Kalendertag (Epoch Day).
*   `completedAt` (Long, NOT NULL): Exakter Zeitstempel des Abschlusses.
*   `status` (String, NOT NULL): Abschlussstatus.
*   *Constraint:* `UNIQUE (habitId, date)` verhindert Duplikate am selben Kalendertag.

### 3. Tabelle: `streak`
Persistierte Aktivitätsserie je Gewohnheit (1:1 Relation, automatische Berechnung).
*   `id` (Long, PK, AutoGenerate): Eindeutiger Primärschlüssel.
*   `habitId` (Long, FK): Verweis auf `habit(id)` mit `ON DELETE CASCADE`. Unique-Index erzwingt 1:1 Beziehung.
*   `currentStreak` (Int, NOT NULL, Default: 0): Aktuelle Serie in Tagen.
*   `bestStreak` (Int, NOT NULL, Default: 0): Historischer Bestwert.
*   `lastActiveDate` (Long?, Nullable): Letzter aktiver Tag (Epoch Day).
*   `updatedAt` (Long, NOT NULL): Letzte Aktualisierung.

---

## 🧪 Qualitätssicherung & Teststrategie

Das System wird durch eine durchgängige Test-Pyramide abgesichert:

### 1. Unit-Tests (Isolierte Komponententests)
*   **Habit-Komponente:** Prüfung der Erstellung, Validierung leerer Namen, Archivierung und korrekter Kaskadierung beim Löschen via Mock-DAOs.
*   **Entry-Komponente:** Sicherstellung der Idempotenz (keine Doppel-Einträge pro Tag) und des Löschens/Rückgängigmachens.
*   **Streak-Komponente:** Verifikation der Berechnungskette (Lückenlose Tage ➔ Erhöhung, ein Fehltag ➔ Abbruch auf 0, Erhalt des historischen Bestwerts).
*   **Statistics & Dashboard:** Korrekte Berechnung von Abschlussquoten (Ausschluss von Division durch Null bei 0 Habits, Heatmap-Generierung).

### 2. Integrationstests (In-Memory DB)
Gemeinsames Zusammenspiel von Repositories und einer echten, im Arbeitsspeicher laufenden Room-Datenbank zur Prüfung von Datenbank-Constraints, Trigger-Verhalten und `ON DELETE CASCADE` Policies.

### 3. End-to-End-Tests (E2E)
Simulation vollständiger Benutzerinteraktionen über die Compose-Oberfläche:
*   Kompletter Lebenszyklus: Habit anlegen ➔ als erledigt markieren ➔ Prüfung, ob Tagesquote steigt, Streak sich erhöht und die Heatmap sich visuell aktualisiert.
*   Zustandsszenarien bei App-Neustarts zur Validierung der echten Offline-Persistenz.
*   Stresstests für das Deaktivieren/Archivieren von Habits.

---

## 📂 Projektstruktur

*   `ui/` — Enthält Jetpack Compose Screens und grafische UI-Komponenten (Dashboard, Habits, Statistics, Heatmap-Widgets).
*   `data/` — Room-Datenbankklasse, DAOs und Entities.
*   `repository/` — Implementierungen und Schnittstellen der Repositories (`HabitRepository`, `EntryRepository`, `StreakRepository`).
*   `viewmodel/` — State-Verwaltung mittels `StateFlow` und Kapselung der Use Cases (`HabitVM`, `StatsVM`, `DashboardVM`).

---

## ✍️ Entwickler
*   **Mohameed Al-Harethi** — *Projektarbeit im Modul Mobile Betriebssysteme und Netzwerke, Juli 2026*
