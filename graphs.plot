set term png small size 1024,768
set output "graphs/test.png"
set size 2.0, 2.0
set multiplot

#packet and drop#
set title "Packets"
set origin 0.0, 0.5
set size 0.5, 0.5
set xlabel "Nodes"
set ylabel "Packets"
set ytics
set grid
plot "graphs/packets.data" using 1 title "packets", \
     "graphs/dropped.data" using 1 title "dropped"

#stream latency#
reset

set title "Stream latencies"
set origin 0.0, 0.0
set size 0.5, 0.5
set xlabel "Stream"
set ylabel "Latency"
#set grid
#set ytics

set table 'graphs/latency-stream-table.data'
plot 'graphs/latency-stream.data' u 1:2
unset table
unset key
f(x) = mean_y
fit f(x) 'graphs/latency-stream.data' u 1:2 via mean_y
stddev_y = sqrt(FIT_WSSR / (FIT_NDF +1 ))
min_y = GPVAL_DATA_Y_MIN
max_y = GPVAL_DATA_Y_MAX

set label 1 gprintf("Mean = %g", mean_y) at 2, min_y
set label 2 gprintf("Standard deviation = %g", stddev_y) at 2, min_y-5

plot mean_y-stddev_y with filledcurves y1=mean_y lt 1 lc rgb "#bbbbdd", \
mean_y+stddev_y with filledcurves y1=mean_y lt 1 lc rgb "#bbbbdd", \
mean_y w l lt 3, 'graphs/latency-stream.data' u 1:2 w p pt 7 lt 1 ps 1

unset label 1
unset label 2
########

#node latency#
reset 
set title "Node latencies"
set origin 0.5, 0.0
set size 0.5, 0.5
set xlabel "Node"
set ylabel "Latency"
#set grid
#set ytics 

set table 'graphs/latency-avg-table.data'
plot "graphs/latency-avg.data" u 1:2
unset table
unset key
f(x) = mean_y
fit f(x) 'graphs/latency-avg.data' u 1:2 via mean_y
stddev_y = sqrt(FIT_WSSR / (FIT_NDF+1))
min_y = GPVAL_DATA_Y_MIN
max_y = GPVAL_DATA_Y_MAX

set label 1 gprintf("Mean = %g", mean_y) at 2, min_y
set label 2 gprintf("Standard deviation = %g", stddev_y) at 2, min_y-10

plot mean_y-stddev_y with filledcurves y1=mean_y lt 1 lc rgb "#bbbbdd", \
mean_y+stddev_y with filledcurves y1=mean_y lt 1 lc rgb "#bbbbdd", \
mean_y w l lt 3, 'graphs/latency-avg.data' u 1:2 w p pt 7 lt 1 ps 1

unset label 1
unset label 2
#############

#bandwidth#
set title "Node bandwidths"
set origin 0.5, 0.5
set size 0.5, 0.5
set xlabel "Nodes"
set ylabel "Bandwidth"
set grid
set ytics
plot "graphs/peak.data" using 1 title "peak", \
     "graphs/avg.data" using 1 title "average"

unset multiplot
set size 2.0, 2.0
