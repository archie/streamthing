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
set title "Stream latencies"
set origin 0.0, 0.0
set size 0.5, 0.5
set xlabel "Stream"
set ylabel "Latency"
set grid
set ytics
plot "graphs/latency-stream.data" using 1:2 title ""

#node latency#
set title "Node latencies"
set origin 0.5, 0.0
set size 0.5, 0.5
set xlabel "Node"
set ylabel "Latency"
set grid
set ytics 
plot "graphs/latency-avg.data" using 1:2 title ""

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
