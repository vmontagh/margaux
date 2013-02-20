#Data to HTML Script

def makeTable(data1, data2):
    dm = open('dm.html', "w")

    dm.write('<html>')
    dm.write('<head>')
    dm.write('<title>Distance Metric Results</title>')
    dm.write('</head>')
    dm.write('<body>')
    dm.write('<table border="1">')
    dm.write('<tr>')
    dm.write('<th>Model</th>')
    dm.write('<th>Distance</th>')
    dm.write('</tr>')
    for i in range(0, len(data1)):
        dm.write('<tr>')
        dm.write('<td>',data1[i],'</td>')
        dm.write('<td>',data2[i],'</td>')
        dm.write('</tr>')
    dm.write('</body>')
    dm.write('</html>')

    dm.close()

"""
data1=['a','b','c','d']
data2=[1,2,3,4]

dm = open('dm.html', "w")

dm.write('<html>')
dm.write('<head>')
dm.write('<title>Distance Metric Results</title>')
dm.write('</head>')
dm.write('<body>')
dm.write('<table border="1">')
dm.write('<tr>')
dm.write('<th>Model</th>')
dm.write('<th>Distance</th>')
dm.write('</tr>')
for i in range(0, len(data1)):
    dm.write('<tr>')
    dm.write('<td>' + str(data1[i]) + '</td>')
    dm.write('<td>' + str(data2[i]) + '</td>')
    dm.write('</tr>')
dm.write('</body>')
dm.write('</html>')

dm.close()
"""
