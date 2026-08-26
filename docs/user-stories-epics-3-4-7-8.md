# **EPIC 3 — Household Membership**

**Goal**: Let registered users form and move between households freely, while the household retains its own history.

**Definition of done**: A user can create a household and take on the role of administrator, other users can join and go, membership is retained during restarts, departures do not erase household history.

**Design decision:** how joining operates. A join code, such as EC4932, is given to a household upon creation and is shared with the other members of the household, as it doesn't require a messaging system, network, or notification infrastructure, all of which are outside the purview of a local desktop application, it was selected over invites, name searches, and admin approval.

## **User story 8 \- Create a household:**

I want to set up a household as a registered user so that everyone in the household may track sustainability data together.

**Acceptance Criteri**a: Household has a valid name. The creator takes on the role of household administrator. Household continues even after the application is restarted.

## **User story 9 \- Join a household**:

I would like to join an existing home as a registered user in order to take part in its sustainability tracking.

**Acceptance Criteri**a: The household join code can be used by the user to join. Membership is recorded. Access to household data is granted to the user. Invalid code displays an error message.

## **User story 10 \- Leave a household:**

As a registered user, I wish to leave a household so that I can stop participating in a household to which I no longer belong.

**Acceptance Criteri**a:The user's household membership is removed. Future access is lost for the user. Leaving does not erase past household data.

## **User story 11 \- View current household members**:

As a registered user, I want to view the current household members so that I can see who is currently involved in the household.

**Acceptance Criteri**a: Members who are active are shown. The household administrator is recognisable. Members who have left are no longer displayed .
---

# **EPIC 4 — Household Activity History**

**Goal:** To help a household understand how its profile changed and who made what changes, keep a data trail of all household modifications.

**Why it matters:** The household's data is constant, but memberships are dynamic people come and go. Administrator corrections are safe rather than harmful because of the history, which also keeps the record consistent over membership changes.

**Definition of done:** data records are created by data modifications, joins, and departures; each record includes the actor, timestamp, action, and, if relevant, the old and new values. History is preserved even after the user who created it leaves.

## **User Story 12 \- View household activity history:**

In order to comprehend how the household profile has evolved over time, as a household member, I would like to see a history of household changes.

**Accredited history:** Changes in the household are noted. Each entry contains the type of change, a time and date, and, if relevant, the name of the responsible user. After users leave, history is retained. The newest entries appear first.

## **User Story 13 \- Record household membership changes:**

In order for membership changes to be visible over time, as a household member, want joins and departures to be documented in the household history.

**Accredited history:** A history entry is created upon joining. A history entry is created after you leave. After the user departs, historical membership entries are still present.

## **User Story 14 \- Record household data changes:**

I want changes to household sustainability data to be documented as a household member so that the household profile's evolution may be tracked.

**Accredited history:** History records are created by pertinent changes. When necessary, original and updated values can be distinguished. The responsible user is identified by the record.

**Data model:** The change log is a separate entity from current household data.

---

# **EPIC 7 — Recommendations**

**Goal:** To inform a household of what needs to be changed so that the score is more than just information.

**Definition of done**: Each recommendation is tagged by domain and is based on the household's own data rather than general advice.

**Dependencies**: E6 is necessary.

## **User story 24 \- Receive sustainability recommendations:**

As a member of the household, I would like EcoTwin to make suggestions based on our household data so that we can determine what adjustments could enhance our sustainability.

**Acceptance Criteria:** Different households have different recommendations based on their data. Every domain has at least one recommendation. Targets the weakest domain first

## **User story 25 \- Identify the domain of a recommendation:**

As a household member, I would like recommendations to be labelled by Energy, Water, Waste, or Transport so that I may determine which area they pertain to.

**Acceptance Criteria:** The domain of each recommendation is shown. Recommendations can be labelled or filtered by domain.

## **User story 26 \- Understand why a recommendation was made:**

As a household member, I would like a brief explanation of EcoTwin's recommendations so that they feel relevant to our home.

**Acceptance Criteria:** The explanation makes reference to the household's own values. Written in plain English.

---
# **EPIC 8 — Scenario Simulation**

**Goal:** Before implementing a change in real life, let a household test it. This is what sets EcoTwin apart from the single-number calculators mentioned in the problem description.

**Definition of done**: Without changing actual household data, a member can model a hypothetical change, compare its score to the current one, and save it for later.

**Dependencies**: E6 is necessary.

## **User story 27 \- Create a sustainability scenario:**

As a household member, I would like to temporarily adjust household values in a scenario in order to investigate potential changes without changing the present household data.

**Acceptance Criteria:** Current household data is the starting point for the scenario. Modifying a scenario value does not create a history entry or change the actual record.

## **User story 28 \- Compare scenario score to current score:**

As a household member, I want to see the potential effects of changes by comparing the scenario score to the current score.

**Acceptance Criteria:** The two scores are displayed. The difference between the two scores is displayed. Makes use of the same computation as the live score. The direction of change is clear.

## **User story 29 \- Save a scenario:**

I wish to preserve a scenario as a household member so that other household members can review possible enhancements at a later time.

**Acceptance Criteria:** The scenario persists after restarting with a user-supplied name and is visible to every member of the household.

## **User story 30 \- View saved scenarios:**

I want to access previously saved scenarios as a household member so that I can go over concepts we have discussed.

**Acceptance Criteria:** Scores and names are displayed for saved scenarios. Selecting one reopens its detail.

## **User story 31 \- Delete a saved scenario:**

I wish to remove out-of-date scenarios from our saved scenario list as a household member.

**Acceptance Criteria:** Verification is required. The scenario has been permanently removed. Household data is unaffected.
