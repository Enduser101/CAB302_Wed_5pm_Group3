notes about refactor
IndicativeScoreCalculator scored all4 domains in private method and only returned the average. US21 needed each domain 
on its own. calculator returns a scorebreakdown now. refactored each domain into it's own class. This al lows us to change domain 
rules without affecting the others 
The trade off is now we have 6 classess instead of 1 