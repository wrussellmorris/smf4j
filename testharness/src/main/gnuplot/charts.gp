set datafile separator ','
set style data lines
set xlabel "Concurrent Writer Threads (counting to 10,000,000)"
set ylabel "Time (ms)"
set terminal png truecolor

set title "Unbounded Counter"
set output "target/images/ub_counter.png"
plot 'hc.csv' using ($0+1):"null_case", \
     ''       using ($0+1):"AtomicLong", \
     ''       using ($0+1):"lc_ub_counter", \
	 ''       using ($0+1):"hc_ub_counter", \
	 ''       using ($0+1):"hpl_counter"

set title "Unbounded Min/Max"
set output "target/images/ub_minmax.png"
plot 'hc.csv' using ($0+1):"null_case", \
     ''       using ($0+1):"lc_ub_min", \
     ''       using ($0+1):"lc_ub_max", \
     ''       using ($0+1):"hc_ub_min", \
     ''       using ($0+1):"hc_ub_max", \

set title "Windowed Counter"
set output "target/images/w_counter.png"
plot 'hc.csv' using ($0+1):"null_case", \
     ''       using ($0+1):"AtomicLong", \
     ''       using ($0+1):"lc_w_10s_counter", \
     ''       using ($0+1):"lc_w_2s_counter", \
     ''       using ($0+1):"hc_w_10s_counter", \
     ''       using ($0+1):"hc_w_2s_counter"

set title "Windowed Min/Max"
set output "target/images/w_minmax.png"
plot 'hc.csv' using ($0+1):"null_case", \
     ''       using ($0+1):"lc_w_10s_counter", \
     ''       using ($0+1):"lc_w_2s_counter", \
     ''       using ($0+1):"hc_w_10s_counter", \
     ''       using ($0+1):"hc_w_2s_counter"
