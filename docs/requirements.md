# EcoTwin — Project Requirements

**Unit:** CAB302 Software Development
**Team:** Earth
**Members:** [Frankie Galea, 11496703 | James Renwick, 12564800 | Edward Trollope ( Ted ), 12367991| Name, Student Number | Name, Student Number | Name, Student Number]
**Version:** 0.2 (draft — Week 5)
**Last updated:** 19 August 2026

---

## 1. Problem statement

Most households want to reduce their environmental impact, but have no clear vision on which changes to make that would make a meaningful difference. Current online calculators give a single number with no explanation and no ability to save results and test a change before committing to it.

EcoTwin is an application that builds a profile of a household's resource use, provides a score, explains which areas contribute most, and recommends improvements. Users can simulate changes, and see its effect on their score before implementing them in real life.

## 2. Target User

Australian households (owners or renters) who want to reduce their utility costs and environmental impact and are willing to spend a few minutes entering data about their home. Assumed to be non-technical and using a computer.

## 3. Scope

### In Scope

- Entering and storing household profile data in the transportation, waste, water, and energy domains.
- Calculating both an overall sustainability score and a breakdown per domain.
- Displaying recommendations that are relevant to the specific household.
- Displaying the score difference that results from simulating hypothetical changes.
- Persisting multiple household profiles and saved scenarios to local storage.

### Out of Scope

Non-goals, not deferred features.
- Cloud storage or cloud synchronisation . 
- Network-dependent functionality.
- Integration with utility providers 
- Network access, cloud sync, or user accounts.
- Mobile or Web Versions.
- Scientifically validated emissions modelling.

## 4. Initial Function Requiremnts
- User authentication - The system will allow users to create an account and log in 
- Energy Tracking - The system will allow users to view and document household energy.
- Waste Tracking - The system will allow users to view and document household waste 
- Water Tracking - The system will allow users to view and document household water
- Transport Tracking - The system will allow users to view and document household transport
- Household Profile - The system will allow users to create and update information about their household 
- Sustainability Score and Dashboard - The system will calculate the sustainability score of the household and display it through visual dashboard

## 5. Initial Non-Functional Requirements
- Usability - the application will be easy to understand and navigate for users who lack a technical background
- Performance - Common actions and sustainability calculation will complete without delay 
- Maintainability - The application should use object-oriented design and separation of concerns to support testing and future development.
- Data Validation - The application should validate user inout and prevent invalid inputs. 
- Accessibility - The application should be readable, clear and be able to communicate important information . 
