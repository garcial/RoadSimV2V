import random
from random import randint

#Border Intersections
intersectionsBn = ['I-CV1501-01', 'I-CV1501-02', 'I-CV149-03', 'I-N340-03', 'I-N340-04']
intersectionsCs = ['I-N340-01','I-CV1520-01', 'I-N340a-01',  'I-CV149-01' ]

algorithms = ['shortest', 'fastest', 'startSmart', 'dynamicSmart']

eventsFile = open("eventos.csv", 'a')

def generateRandomSample(startinHour, finalHour, num, algorithmType=None):

	for x in range(num):

		if(randint(0,2) == 0):
 			A = intersectionsCs
 			B = intersectionsBn
		else:
			A = intersectionsBn
			B = intersectionsCs
		start = random.choice(A)
		end = random.choice(B)

		while (start == end):
			end = random.choice(B)

		hour = randint(startinHour, finalHour)
		minute = randint(0, 59)

		speed = randint(70, 100)

		if algorithmType == None:

			algorithm = random.choice(algorithms)
		else:
			algorithm = algorithmType

		eventsFile.write("newCar," + str(hour).zfill(2) + ":" + str(minute).zfill(2) + "," + start + "," + end +
			"," + str(speed) + "," + algorithm + "\n")

#All day
print('Genera datos desde las 18:00 hasta las 19:00')
generateRandomSample(18, 18, 2500)