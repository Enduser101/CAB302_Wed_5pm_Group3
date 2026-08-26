# **EPIC 5 — Household Resource Tracking**
**Goal:**  The goal is to give household members a way to log consumption across all four domains, so the sustainability score is actually backed by real data not placeholders
**Definition of done:**All four domains accept deal updates. Invalid input is rejected with a message that helps the user understand what went wrong. datta belongs to the household. Every change recalculates the score and writes a history record
**Dependencies:**  Requires E3 and E4. Blocks E6 without data there is no score 
**Sequencing Note:** Build User story 15 first. it establishes the input form, validations,persistence and history logging pattern that the other three domains use, so user stories 16 through 18 should go considerably faster 

## **User story 15 \- Record energy information:**
As a household member, I want to update the household's energy information so that EcoTwin can assess our energy sustainability.
**Acceptance Criteria:** Valid values can be entered. Invalid values are rejected with a helpful message. Data belongs to the household. Data persists. Score recalculates. Change is recorded in household history.

## **User Story 16 \- Record water information:**

As a household member, I want to update the household's water information so that EcoTwin can assess our water sustainability.

**Acceptance Criteria:** Valid information can be saved. Data belongs to the household. Data persists. Score recalculates. Change is logged.

## **User Story 17 \- Record waste information:**
As a household member, I want to update the household's waste and recycling information so that EcoTwin can assess our waste sustainability.

**Acceptance Criteria:** Valid values can be saved. Data persists. Score recalculates. Change is logged.

## **User Story 18 \- Record transport information:**
As a household member, I want to update household transport information so that EcoTwin can assess the sustainability impact of our transport behaviour.

**Acceptance Criteria:** Valid information can be saved. Data persists. Score recalculates. Change is logged.

## **User Story 19 \- Edit household sustainability information:**

As a household member, I want to modify existing resource information so that EcoTwin reflects changes in our household behaviour.

**Acceptance Criteria:** Current values are displayed. Valid edits can be saved. Previous state remains represented in activity history. Scores recalculate.

# **EPIC 6 — Sustainability Scores**
**Goal:** Turn recorded data into a score and a breakdown, so that a household can see where it stands and which area matters most.
**Definition of done:** An overall score and four domain scores display on a dashboard. The score's meaning is explained. Changes in score after an update are visible. Calculation completes without noticeable delay.
**Dependencies:** Requires E5. Blocks E7 and E8 both operate on the score.

## **User story 20 \- View overall sustainability score:**
As a household member, I want to view our overall sustainability score so that I can quickly understand the household's environmental performance.
**Acceptance Criteria:** Single overall figure displayed. Reflects all household data. Recalculates when data changes.

## **User Story 21 \- View domain scores:**

As a household member, I want to view separate Energy, Water, Waste and Transport scores so that I can identify areas requiring improvement.

**Acceptance Criteria:** Four domain scores shown alongside the total. Presented visually, not as raw numbers alone. Meaning is not conveyed by colour alone.

## **User Story 22 \- Understanding score meaning :**
As a household member, I want an explanation of what our score represents so that I can interpret the result meaningfully.

**Acceptance Criteria:** Scale and its bounds are stated. Explanation is written for a non-technical reader.

## **User Story 23 \- See changes in score after updating household data:**
As a household member, I want to see how updates affect our sustainability score so that I understand the impact of changes in household behaviour.

**Acceptance Criteria:** Previous and new score both shown after an update. Affected domain score also shown. Direction of change is unambiguous.

User story 23 pairs naturally with the activity history. An entry can show both the data chnage and its score consequences: 

|               | Before | After  |
|---------------|--------|--------|
| Energy use    | 850kWh | 700kWh |
| Energy score  | 62     | 71     |
| Overall score | 68     | 71     |
 