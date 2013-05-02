set datafile separator ','
set style data linespoints
set terminal pngcairo color dashed truecolor
set key top left

# define axis
# remove border on top and right and set color to gray
set style line 11 lc rgb '#808080' lt 1
set border 3 back ls 11
set tics nomirror
# define grid
set style line 13 lc rgb '#808080' lt 0 lw 1
set grid back ls 13
set style line 1 lc rgb '#8b1a0e' pt 7 ps 1 lt 1 lw 2 # --- red
set style line 2 lc rgb '#5e9c36' pt 7 ps 1 lt 1 lw 2 # --- green
set style line 3 lc rgb '#0033CC' pt 7 ps 1 lt 1 lw 2 # --- dark blue
set style line 4 lc rgb '#CC6600' pt 7 ps 1 lt 1 lw 2 # --- orange
set style line 5 lc rgb '#993399' pt 7 ps 1 lt 1 lw 2 # --- purple
set style line 6 lc rgb '#808033' pt 7 ps 1 lt 1 lw 2 # --- crap yellow
set style line 7 lc rgb '#8b1a0e' pt 7 ps 1 lt 2 lw 2 # --- red
set style line 8 lc rgb '#5e9c36' pt 7 ps 1 lt 2 lw 2 # --- green
set style line 9 lc rgb '#0033CC' pt 7 ps 1 lt 2 lw 2 # --- dark blue
set style line 10 lc rgb '#CC6600' pt 7 ps 1 lt 2 lw 2 # --- orange
set style line 11 lc rgb '#993399' pt 7 ps 1 lt 2 lw 2 # --- purple
set style line 12 lc rgb '#808033' pt 7 ps 1 lt 2 lw 2 # --- crap yellow

set xtics 1
set xlabel "Concurrent Writer Threads (counting to 10,000,000)"
set ylabel "Time (ms)"

set title "Unbounded Counter"
set output "target/images/ub_counter.png"
plot 'hc.csv' using "writers":"null_case":xtic(1) ls 1, \
     ''       using "writers":"lc_ub_g_counter":xtic(1) ls 2, \
     ''       using "writers":"hc_ub_g_counter":xtic(1) ls 4, \
     ''       using "writers":"hc_ub_p_counter":xtic(1) ls 10, \
     ''       using "writers":"hpl_counter":xtic(1) ls 5

set title "Unbounded Min/Max"
set output "target/images/ub_minmax.png"
plot 'hc.csv' using ($0+1):"null_case":xtic(1) ls 1, \
     ''       using ($0+1):"lc_ub_g_min":xtic(1) ls 2, \
     ''       using ($0+1):"lc_ub_g_max":xtic(1) ls 3, \
     ''       using ($0+1):"hc_ub_p_min":xtic(1) ls 8, \
     ''       using ($0+1):"hc_ub_p_max":xtic(1) ls 9, \

set title "Windowed Counter"
set output "target/images/w_counter.png"
plot 'hc.csv' using ($0+1):"null_case":xtic(1) ls 1, \
     ''       using ($0+1):"lc_w_g_10s_counter":xtic(1) ls 2, \
     ''       using ($0+1):"lc_w_g_2s_counter":xtic(1) ls 3, \
     ''       using ($0+1):"hc_w_p_10s_counter":xtic(1) ls 8, \
     ''       using ($0+1):"hc_w_p_2s_counter":xtic(1) ls 9

set title "Windowed Min/Max"
set output "target/images/w_minmax.png"
plot 'hc.csv' using ($0+1):"null_case":xtic(1) ls 1, \
     ''       using ($0+1):"lc_w_g_10s_min":xtic(1) ls 2, \
     ''       using ($0+1):"lc_w_g_2s_min":xtic(1) ls 3, \
     ''       using ($0+1):"lc_w_g_10s_max":xtic(1) ls 4, \
     ''       using ($0+1):"lc_w_g_2s_max":xtic(1) ls 5, \
     ''       using ($0+1):"hc_w_p_10s_min":xtic(1) ls 8, \
     ''       using ($0+1):"hc_w_p_2s_min":xtic(1) ls 9, \
     ''       using ($0+1):"hc_w_p_10s_max":xtic(1) ls 10, \
     ''       using ($0+1):"hc_w_p_2s_max":xtic(1) ls 11
