**EPIC 3 — Household Membership**

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

