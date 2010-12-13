set xlabel "Nodes"
set ylabel "Packets"
set ytics
set grid
set output "graphs/packets.png"
plot "packets.data" using 1 title "packets"
