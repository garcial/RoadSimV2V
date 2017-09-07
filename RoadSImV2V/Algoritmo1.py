import random
from random import randint

#Border Intersections
intersectionsBn = ['I-CV1501-01', 'I-CV1501-02', 'I-CV149-03', 'I-N340-03', 'I-N340-04']
intersectionsCs = ['I-N340-01','I-CV1520-01', 'I-N340a-01',  'I-CV149-01' ]

algorithms = ['shortest', 'fastest', 'startSmart', 'dynamicSmart']

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
		
speed = randint(80, 120)

percentNoSmart = [50,37,37,37,25,25,25,0,0,0]
percentStartSmart = [0,13,0,26,25,0,50,50,100,0]
percentDynamicSmart = [0,13,26,0,25,50,0,50,0,100]

def generateRandomSample(startinHour, finalHour, num, start, end, speed, algorithmType=None, pNoSmart, pStartSmart, pDynamicSmart):

	nameFileSmart = ""+ pNoSmart + "NoS"
	if(pStartSmart == pDynamicSmart):
		nameFileSmart += pStartSmart + "s"
	else:
		nameFileSmart += pStartSmart + "sS" + pDynamicSmart + "dS"

	eventsFile = open(nameFileSmart , 'a')
	
	for x in range(num):
	
		hour = randint(startinHour, finalHour)
		minute = randint(0, 59)
		
		intAlgorithm = randint(0, 100)
		algorithm = algorithmType

		eventsFile.write("newCar," + str(hour).zfill(2) + ":" + str(minute).zfill(2) + "," + start + "," + end +
			"," + str(speed) + "," + algorithm + "\n")

#All day
print('Genera datos desde las 18:00 hasta las 19:00')
for i in range(len(percentNoSmart)):
	generateRandomSample(18, 18, 2500, start, end, speed, percentNoSmart[i], percentStartSmart[i], percentDynamicSmart[i])