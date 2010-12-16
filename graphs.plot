set xlabel "Nodes"
set ylabel "Packets"
set ytics
set grid
set terminal png
set output "graphs/packets.png"
plot "dropped.data" using 1 title "dropped", \
 "packets.data" using 1 title "packets"
