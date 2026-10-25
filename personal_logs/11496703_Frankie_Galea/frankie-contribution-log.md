# Individual Weekly Log — Frankie Galea
Student Number: 11496703

---

## Summary

**Code** — delivered US-05 and US-06 (view account details, change password), US-13 (record household membership changes) and the score-over-time half of US-23. Wrote 9 unit tests, two features test-first with separate red and green commits. Added `UserDao.updatePassword`, `HouseholdScoreService.scoreHistory` and the `ScorePoint` record, plus the UI for each. Refactored the duplicated scenario construction in `HouseholdScoreService` with the test suite unchanged as proof of behaviour, and tidied the test setup in the tests I owned.

**Requirements** — wrote the user stories, goals, definitions of done and acceptance criteria for Epic 3 (Household Membership, US-08 to US-11) and Epic 4 (Household Activity History, US-12 to US-14).

**Design** — produced most of the high fidelity design set, around 15 screens across Dashboard, Resources, Recommendations, Scenarios, Activity History, Create/Join Household and Account.

**Week 9 submission video** — built the medium fidelity, high fidelity and design-to-build slides, wrote the script, recorded the high fidelity and design-to-build segment, and assembled the final video from the team's recorded segments.

**Final submission video** — prepared the demo environment, wrote the script, recorded the one-minute demo of the finished application, and assembled the final video from the team's recorded segments.

---

## 19/08/2026

### Tutorial and Team Meeting

* Attended the tutorial and team meeting where the project setup, user stories and UI direction were agreed.
* Contributed to the initial user story discussion and the division of epics between team members.
* Agreed that Figma would be used for UI design and took on the visual design work.

---

## 23/08/2026

### Team Meeting

* Attended the team meeting to review and improve the EcoTwin user stories.
* Contributed to the discussion on removing the premium-user concept, on the grounds that there is no payment system in a local desktop application and it would split permissions the team would then have to build.
* Discussed how household permissions should work and how the epics would be divided.

---

## 25–31/08/2026

### Epic 3 — Household Membership (US-08 to US-11)

* Wrote the user stories for creating, joining and leaving a household, and for viewing current household members.
* Defined the epic goal, definition of done and acceptance criteria for each story.
* Took the constraint Tinom raised at the 19/08 meeting — that a household administrator cannot leave without first transferring administrator rights — and wrote it into the acceptance criteria and the permissions matrix, since the household would otherwise be left with nobody able to manage members.

### Epic 4 — Household Activity History (US-12 to US-14)

* Wrote the user stories for viewing household activity history, recording membership changes and recording data changes.
* Established the rationale for the activity log: household membership is fluid as people move in and out, but the household's resource data is continuous, so the history is what keeps the record coherent across joins and departures.
* Specified the change log as a separate entity from current household data, holding user, timestamp, action, old value and new value.
* Identified a conflict between US-36 (delete household) and the Epic 4 principle that history persists, and raised it as an open decision for the team rather than resolving it unilaterally.

### Design Decision — Household Join Mechanism

* Compared four options for how a user joins an existing household: invitations, name search, administrator approval and join codes.
* Chose join codes on the grounds that the alternatives all require messaging, networking or notification infrastructure, none of which exist in a local desktop application with no network access.
* Specified a six-character code using an alphabet that excludes visually ambiguous characters, since codes are read aloud and typed from memory.

### Role Definitions and Permissions

* Contributed to defining the application's roles as guest, registered user and household administrator.
* Argued against a system-level administrator role on the grounds that a local, network-free application has no shared user base to administer, and that including it would mean being assessed against backlog the team never intended to deliver.
* Built a permissions matrix mapping every action against the three roles, so that each denied permission became an identifiable negative test case.

---

## 29/08/2026

### Team Meeting

* Attended the team meeting covering sprint task delegation and the split of Figma design work between team members.

---

## 01–07/09/2026

### Dashboard — High Fidelity

* Agreed a component set with the team (sidebar, cards, buttons, spacing scale) in the shared Figma file before any screen was drawn, so that five people designing separately would not produce five visual languages.
* Built the Dashboard screen at 1440×960 with a 280px sidebar, using Auto Layout so the four domain score cards distribute evenly rather than being positioned by hand.
* Created reusable components for the domain card, navigation item and button so they could be reused across the remaining screens.
* Designed the overall score card, the score-over-time panel, the four domain cards, the weakest-area callout and the action row.

### Accessibility Design Decision

* Added letter grades alongside every numeric score (for example "71 — B · Good") so the score communicates without relying on colour.
* This directly addresses the project's accessibility non-functional requirement, which states that the application must communicate important information clearly; a red/amber/green score alone would fail for a colourblind user or in greyscale.

### Palette Application

* Applied the team's Mint & Leaf dark palette to the Dashboard.
* Raised that reserving green for identity and navigation, rather than using it throughout, leaves amber free to flag the weakest-scoring domain — otherwise the most meaningful colour in a sustainability application is spent on decoration.

### Resources — High Fidelity

* Designed the Resources screen with the four domains as tabs rather than nested navigation items, so that the structure maps directly onto a JavaFX `TabPane` and entering a reading takes fewer clicks.

---

## 08/09/2026

### Recommendations — High Fidelity

* Designed the Recommendations (All) page as a single ranked list rather than domain tabs, so the highest-impact recommendation is visible on landing instead of hidden behind a tab.
* Ordered recommendations by expected point gain, descending, so the ordering itself satisfies the requirement to target the weakest domain first.
* Designed each recommendation card to carry a domain label, the action, a reason drawn from the household's own figures, and the expected score gain.
* Designed the Energy tab filtered state, using filter chips with "All" as the default rather than tabs.

### Resources — High Fidelity

* Completed the Resources page layout, including the current-score panel beside the form so the user can see what they are affecting as they type.
* Designed the "last updated by" panel, which surfaces the Epic 4 activity history in the interface rather than leaving it as a database table nobody sees.

---

## 09/09/2026

### Resources — Remaining Domain Tabs

* Designed the Water tab, setting quarterly as the default billing period because Australian water bills are issued quarterly.
* Made household size read-only context on the Water tab with a link to the Household page, rather than a duplicate input, to avoid two sources of truth for the same value.
* Designed the Waste tab using bin size, fullness and collection frequency rather than asking users to weigh their rubbish, since those are values a household can actually report.
* Designed the Transport tab as a repeating group with add and remove actions, since a household may have zero or several vehicles, unlike the fixed forms used by the other three domains.
* Designed validation error states across the tabs, including a numeric range error and a negative-value error.

### Account — High Fidelity

* Designed the Account page with read-only detail rows visually distinct from editable inputs, so it is clear which values can be changed.
* Designed the change-password form including a password-mismatch error state, giving the design set a second distinct validation pattern.

---

## 10/09/2026

### Recommendations — Domain Filtered States

* Designed the Transport, Water, Energy and Waste filtered views of the Recommendations screen as separate frames, demonstrating the filtering behaviour without needing a working prototype.
* Kept the explanatory reasoning on every card in every filter state, since that reasoning is what separates the feature from a list of generic sustainability tips.

### Activity History — High Fidelity

* Designed the Activity History screen as a chronological list with timestamps in their own column, newest first.
* Included a correction pair in the demo data, showing an administrator's correction written on top of an original entry with both still visible, rather than the original being erased. This makes the Epic 4 design principle visible in the interface.
* Included a departed member's entries remaining in the log after they leave, demonstrating that history survives membership changes.

### Scenarios — High Fidelity

* Designed the Saved Scenarios list showing each scenario's name, domain, author, date, score delta and resulting score.
* Showed both the delta and the absolute resulting score, since the delta allows ranking while the absolute score shows where the household would end up.
* A team member had also produced a version of this screen; raised it rather than quietly replacing theirs.

---

## 13/09/2026

### Create / Join Household — High Fidelity

* Designed the household setup screen as two side-by-side options, since a new user does not know which applies to them until they can see both described.
* Styled the join code input in monospace with letter spacing, because the code is shared verbally and typed from memory.
* Designed the invalid join code error state, giving the design set a third distinct validation pattern.
* Designed the disabled-navigation state for a registered user who has not yet joined a household, since five of the six navigation items have nothing to show until a household exists.

### Design Handover

* Exported the complete high fidelity design set to PDF.
* Uploaded the export to the repository so the designs are versioned alongside the code rather than existing only behind a Figma link.

---

## 15/09/2026

### US-05 and US-06 — Account Details and Change Password

* Reviewed the existing `AuthService` and found registration and login already implemented, so scoped the work to the change-password path and the account details view.
* Added `updatePassword` to the `UserDao` interface and implemented it in `SqliteUserDao`, matching the existing error-handling style in that class.
* Implemented `AuthService.changePassword`, verifying the current password with BCrypt before validating the new one, so that password rules cannot be probed without being authenticated.
* Used a specific error message for an incorrect current password rather than the generic message used at login, since the user is already authenticated and there is nothing to disclose.
* Extracted password length validation into a shared `validatePassword` method so the rule lives in one place.

### Testing

* Wrote four JUnit 5 and Mockito tests covering the change-password acceptance criteria: incorrect current password rejected, short new password rejected, the stored value is a hash rather than plain text, and the old password no longer authenticates after a change.
* Confirmed the full suite passed at 19 tests.

### Account User Interface

* Implemented the account details view in `AccountController` and `account-view.fxml`, formatting the stored ISO timestamp into a readable "member since" date.
* Implemented the change-password form with its error states.
* Styled the page to match the high fidelity design, adding the `section-title` and `value-text` styles to the shared stylesheet.

### Git Workflow

* Pushed to `feature/account-change-password` and opened a pull request into `dev`.
* Noted in the pull request that `UserDao` is a shared interface and had gained a method, so that any other implementation would need updating.

---

## 16/09/2026

### Submission Video — Slides

* Built the medium fidelity, high fidelity and design-to-build slides for the Week 9 submission video, in the team's established slide style.
* Included a design-to-build slide showing the high fidelity Account frame beside the same screen running in the application, as direct evidence that the designs fed the implementation rather than sitting unused.
* Wrote the narration script for the UI design segment.
* Added the artefact repository path to each slide, since the assessment criteria state that evidence which cannot be located in the submission may not be assessed.

---

## 20/09/2026

### Submission Video — Recording and Assembly

* Recorded the high fidelity and design-to-build segment.
* Edited and assembled the full submission video from the team's separately recorded segments.

### Team Meeting

* Attended the team meeting.

---

## 30/09/2026

### Tutorial

* Attended the tutorial. No other group members were present; Michael had attended earlier and left for the same reason.

---

## 04/10/2026

### Team Meeting

* Attended the team meeting.

---

## 07/10/2026

### US-13 — Record Household Membership Changes

* Reviewed the three acceptance criteria against the existing implementation and found that joining was already logged in `joinHousehold`, so scoped the work to the departure path and test coverage for all three.
* Wrote two failing tests in `HouseholdServiceTest` first: one asserting an activity-log entry when a member leaves, one asserting that joining then leaving produces two separate entries on the same household.
* Committed the failing tests separately as the red step.
* Added the departure logging to `HouseholdService.leaveHousehold` and committed that as the green step, so the red–green cycle is visible in the commit history.
* Confirmed the full suite passed at 65 tests.

### Design Reasoning

* Established that the third acceptance criterion — history persists after a member leaves — holds by construction rather than by test: `ActivityLogDao` exposes only a `log` method, so there is no deletion path, and `leaveHousehold` ends the membership without touching the log.
* Stated this in the pull request rather than claiming a test proved it.
* Noted that the Epic 9 administration actions should log the same way once implemented, so whoever picks those up has the context.

### Git Workflow

* Pushed to `feature/us-13-membership-history` and opened a pull request into `dev`.
* The pull request was initially raised against `main` and was corrected to target `dev`.

---

## 13/10/2026

### Story Review — US-20

* Reviewed the `US-21-domain-scores` branch before starting assigned work on US-20 (view overall sustainability score).
* Found that the Dashboard built for US-21 already displayed the overall score with its grade, satisfying all three of US-20's acceptance criteria.
* Raised this with the team rather than duplicating completed work, and took on the score-over-time story instead.

### Bug Triage — Scenario Score Comparison

* Investigated a reported bug where applying a large change in the Scenario Simulator produced "No change from your current score".
* Traced the behaviour to the score floor rather than a fault in the scenario comparison: each category's sub-score is capped, so a household already at the minimum cannot score lower.
* Confirmed with a three-point comparison across different electricity values, showing the score plateauing rather than failing to update.
* Reproduced correct behaviour with typical household data, where the same change produced a visible drop and a "worse" label.
* Recommended closing the issue as working as designed and raising a follow-up for the message wording, which reads as though the scenario was not applied.

---

## 14/10/2026

### US-23 — Score Over Time

* Took the score-over-time half of US-23; the before-and-after score change was built separately by another team member.
* Reviewed the existing scoring architecture and established that score history could be derived from the readings already stored, since every entry carries a `YYYY-MM` period.
* Chose to compute history from stored readings rather than add a score-history table, so there is no schema migration and no cached score that could drift from the data it came from.
* Wrote three failing tests first in `HouseholdScoreServiceTest`: one period per complete set of readings in oldest-first order, periods missing a domain reading being left out, and a household with no readings returning no history.
* Committed the failing tests and the new `ScorePoint` record separately as the red step.
* Implemented `HouseholdScoreService.scoreHistory` with supporting private helpers to collect the periods present across all four domains and build a scenario for each.
* Applied the same completeness rule as the existing `currentScores` method, so a period missing any domain reading is skipped rather than scored on partial data.
* Confirmed the full suite passed at 132 tests.

### Dashboard Integration

* Replaced the score-over-time placeholder on the Dashboard with a working `LineChart`.
* Added a change summary beneath the chart showing the movement since the first recorded period.
* Hid the chart until the household has two periods of readings, since a single point does not form a line, and replaced it with guidance to record another month.
* Styled the chart in the shared stylesheet to match the high fidelity design, removing axis furniture and grid lines.

### Verification

* Verified the feature end to end in the running application by back-dating readings in the development database so a second period existed, then confirmed the chart rendered with the correct periods and values.

### Git Workflow

* Pushed to `feature/us-23-score-chart` and opened a pull request into `dev`.

---

## 18/10/2026

### Team Meeting

* Attended the team meeting.

---

## 19–20/10/2026

### Assignment 3 — Key Lessons and Professional Insights

* Prepared the closing section of the final presentation.
* Drafted the topic titles first and shared them with the team, so the five sections could be checked against each other before anyone wrote a script.
* Read the other four members' talking points and rewrote mine where they overlapped, since the team's process, architecture and quality sections already covered ground my first draft repeated.
* Built the slide for the section.
* Cut the script to roughly 160 words to match the length of the other sections and the time available.
* Prepared for the Q&A, including likely questions on my own section and on the parts of the project presented by others.

---

## 21/10/2026

### Group Rehearsal

* Rehearsed the full presentation with the team, running the handovers between speakers and checking the overall timing.

### Assignment 3 — Presentation and Oral Assessment

* Presented the Key Lessons and Professional Insights section and closed the presentation.

---

## 23/10/2026

### Refactor — Scenario Construction in HouseholdScoreService

* Identified duplication introduced by my own US-23 work: `currentValues` and `scenarioFor` both constructed a `SustainabilityScenario` from the same eight values in the same order, so any change to the scenario's shape would have had to be made twice.
* Extracted the construction into a single private `scenarioFrom` method and pointed both call sites at it.
* Made no changes to any test, so the existing suite stands as the proof that behaviour is unchanged. The full suite passed at 169 tests before and after.
* Committed the production refactor on its own, separate from the test changes below, so the refactor step is legible in the commit history rather than mixed in with other work.

### Test Refactor — HouseholdScoreServiceTest

* Moved the repeated Mockito stubbing in my three `scoreHistory` tests into named helpers (`givenEnergy`, `givenWater`, `givenWaste`, `givenTransport`, `givenNoReadings`), so each test states only the condition it is testing.
* Added factory methods for each entry type, removing the positional `null` padding that made the entry constructors hard to read at a glance.
* Scoped the change to the three tests I wrote. The `@BeforeEach` and the `currentValues` tests above them were written by another team member and were left untouched, with a note in the pull request offering to apply the same helpers if the author wants them.
* Confirmed the suite still passed at 169 tests.

### Git Workflow

* Pushed to `refactor/scenario-construction` as two commits — production refactor, then test refactor — and opened PR #133 into `dev`, which was reviewed and merged.

---

## 24/10/2026

### Final Submission Video — Demo Segment

* Prepared the demo environment using the seeded demo account rather than hand-entered data, giving six months of readings across all four domains and a backdated edit history, so the score-over-time chart and the activity history both had real content to show.
* Investigated why no water recommendation appeared for the demo household. Traced it to the `CutWater` rule, which fires above 5,500 litres per person per month; the household's October reading of 9,500 litres across two occupants is 4,750 per person, below the benchmark.
* Concluded the behaviour was correct and left the seeded data as it was, rather than inflating the water reading to force a recommendation into the demo.
* Wrote and recorded the one-minute demo segment covering the dashboard, score over time, recommendations, scenarios and account details, with emphasis on what was built after the Week 9 submission.

---

## 25/10/2026

### Final Submission Video — Assembly

* Edited and assembled the full final submission video from the team's separately recorded segments, in the order set by the assessment's recommended sequence: overview, demo, Java code walkthrough, continuous integration, feedback-driven improvement.
* Checked the section timings against the suggested allocations and that the transitions between speakers ran cleanly.
