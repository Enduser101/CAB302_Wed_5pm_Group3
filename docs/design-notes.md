notes about refactor
IndicativeScoreCalculator scored all4 domains in private method and only returned the average. US21 needed each domain 
on its own. calculator returns a scorebreakdown now. refactored each domain into it's own class. This al lows us to change domain 
rules without affecting the others 
The trade off is now we have 6 classess instead of 1 

notes about refactor (US-23)
US-23 needed the same "+2.8 points" wording the scenario page (US-28) already built in its controller. Rather than copy it into
ResourcesController, the wording moved into ScenarioComparison.signedPoints() and both controllers use it, so the score
difference reads the same everywhere. It is now in the model, so it is unit tested (ScenarioComparisonTest) instead of only
being visible in the UI. ScoreChange reuses ScenarioComparison for its before/after scores rather than repeating the
rounding and direction rules.
