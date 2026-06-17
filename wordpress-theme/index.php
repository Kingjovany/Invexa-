<?php
/**
 * Invexa Financial Theme Main Dashboard Template
 *
 * @package InvexaTheme
 */

get_header();

// Fetch all saved Investment custom posts
$args = array(
	'post_type'      => 'investment_lockup',
	'posts_per_page' => -1,
	'post_status'    => 'publish',
);
$lockups_query = new WP_Query( $args );

// Calculate cumulative stats to feed the dashboard just like our Compose companion!
$total_capital  = 0;
$total_maturity = 0;
$active_count   = 0;
$progress_sum   = 0;

if ( $lockups_query->have_posts() ) {
	while ( $lockups_query->have_posts() ) {
		$lockups_query->the_post();
		$post_id = get_the_ID();

		$capital = (float) get_post_meta( $post_id, 'capital_locked', true );
		$rate    = (float) get_post_meta( $post_id, 'interest_rate', true );
		$days    = (int) get_post_meta( $post_id, 'duration_days', true );
		$start_t = strtotime( get_post_meta( $post_id, 'start_date', true ) ?: date('Y-m-d') );

		$earnings = $capital * ( $rate / 100.0 ) * ( $days / 365.25 );
		
		$total_capital  += $capital;
		$total_maturity += ( $capital + $earnings );
		$active_count++;

		// Progress calculation based on current time
		$span_sec = $days * 86400;
		$elapsed  = time() - $start_t;
		$progress = $span_sec > 0 ? clamp_float( $elapsed / $span_sec, 0.0, 1.0 ) : 1.0;
		$progress_sum += $progress;
	}
	wp_reset_postdata();
}

$average_progress = $active_count > 0 ? ($progress_sum / $active_count) * 100 : 0;

function clamp_float($v, $min, $max) {
	return max($min, min($max, $v));
}
?>

<main class="flex-grow container mx-auto px-6 py-10 max-w-6xl">

	<!-- Dashboard Interactive Cover and Guide Banner -->
	<div class="bg-gradient-to-r from-slate-900 via-slate-800 to-indigo-950 p-8 rounded-3xl border border-gray-800 mb-10 shadow-2xl relative overflow-hidden">
		<div class="absolute right-0 bottom-0 opacity-10 transform translate-x-12 translate-y-12">
			<svg class="h-96 w-96 text-white" stroke="currentColor" fill="none" viewBox="0 0 24 24">
				<path stroke-linecap="round" stroke-linejoin="round" stroke-width="1" d="M11 3.055A9.001 9.001 0 1020.945 13H11V3.055z" />
				<path stroke-linecap="round" stroke-linejoin="round" stroke-width="1" d="M20.488 9H15V3.512A9.025 9.025 0 0120.488 9z" />
			</svg>
		</div>

		<div class="relative z-10 max-w-2xl">
			<span class="bg-green-500 bg-opacity-25 text-green-400 font-extrabold text-xs px-3 py-1.5 rounded-full uppercase tracking-widest">WordPress Theme Template ready</span>
			<h2 class="text-3xl md:text-4xl font-extrabold mt-4 text-slate-100 tracking-tight leading-none">
				Active Investment Lockups Dashboard
			</h2>
			<p class="text-sm text-gray-400 mt-3 leading-relaxed">
				This theme mirrors the precise business logic and dark aesthetic of the Invexa Android Application. Create, update, or remove live microfinance portfolios directly via standard custom post fields inside WordPress.
			</p>
			
			<div class="mt-6 flex flex-wrap gap-4">
				<a href="#how-to-use" class="bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-bold px-5 py-3 rounded-xl transition border border-gray-700">
					Theme Setup Instructions &rarr;
				</a>
			</div>
		</div>
	</div>

	<!-- Core Financial Dashboard Counters (Identical logic as Jetpack Compose state!) -->
	<section class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-10">
		
		<div class="bg-slate-900 border border-gray-800 p-6 rounded-2xl shadow-lg flex flex-col justify-between">
			<div class="flex items-center justify-between mb-4">
				<span class="text-xs uppercase font-extrabold text-gray-400 tracking-wider">Active Lockups</span>
				<div class="p-2 rounded-lg bg-green-500 bg-opacity-10 text-green-400">
					<svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
						<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
					</svg>
				</div>
			</div>
			<div>
				<span class="block text-2xl font-black text-slate-100"><?php echo esc_html($active_count); ?></span>
				<span class="text-10px text-gray-500 mt-1 block uppercase">Active subscriptions in Cameroon</span>
			</div>
		</div>

		<div class="bg-slate-900 border border-gray-800 p-6 rounded-2xl shadow-lg flex flex-col justify-between">
			<div class="flex items-center justify-between mb-4">
				<span class="text-xs uppercase font-extrabold text-gray-400 tracking-wider">Total locked capital</span>
				<div class="p-2 rounded-lg bg-blue-500 bg-opacity-10 text-blue-400">
					<svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
						<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
					</svg>
				</div>
			</div>
			<div>
				<span class="block text-2xl font-black text-slate-100"><?php echo number_format($total_capital); ?> FCFA</span>
				<span class="text-10px text-gray-500 mt-1 block uppercase">Initial state deposit bound</span>
			</div>
		</div>

		<div class="bg-slate-900 border border-gray-800 p-6 rounded-2xl shadow-lg flex flex-col justify-between">
			<div class="flex items-center justify-between mb-4">
				<span class="text-xs uppercase font-extrabold text-gray-400 tracking-wider">Harvest yield projection</span>
				<div class="p-2 rounded-lg bg-emerald-500 bg-opacity-10 text-emerald-400">
					<svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
						<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
					</svg>
				</div>
			</div>
			<div>
				<span class="block text-2xl font-black text-emerald-400">+<?php echo number_format($total_maturity - $total_capital); ?> FCFA</span>
				<span class="text-10px text-gray-500 mt-1 block uppercase">Calculated net yield payout</span>
			</div>
		</div>

		<!-- Combined aggregate lockups slider progress indicator -->
		<div class="bg-slate-900 border border-gray-800 p-6 rounded-2xl shadow-lg flex flex-col justify-between">
			<div class="flex items-center justify-between mb-2">
				<span class="text-xs uppercase font-extrabold text-slate-300 tracking-wider">Aggregated progress</span>
				<span class="text-xs font-extrabold text-green-400"><?php echo number_format($average_progress, 1); ?>%</span>
			</div>
			
			<div class="w-full bg-slate-800 rounded-full h-2.5">
				<div class="bg-gradient-to-r from-green-500 to-indigo-505 h-2.5 rounded-full" style="width: <?php echo esc_attr($average_progress); ?>%; background-color: #10B981;"></div>
			</div>

			<div class="mt-2">
				<span class="text-10px text-gray-500 uppercase block leading-none">Cumulative maturity factor</span>
			</div>
		</div>

	</section>

	<div class="grid grid-cols-1 lg:grid-cols-3 gap-10">
		
		<!-- Left: Primary WordPress Loop rendering Active portfolios with dynamic calculations -->
		<div class="lg:col-span-2 space-y-6">
			<h3 class="text-xl font-bold uppercase tracking-wider text-slate-200">Active portfolio instances</h3>

			<?php if ( $lockups_query->have_posts() ) : ?>
				<?php while ( $lockups_query->have_posts() ) : $lockups_query->the_post(); 
					$post_id = get_the_ID();
					$capital = (float) get_post_meta( $post_id, 'capital_locked', true );
					$rate    = (float) get_post_meta( $post_id, 'interest_rate', true );
					$days    = (int) get_post_meta( $post_id, 'duration_days', true );
					$start_s = get_post_meta( $post_id, 'start_date', true ) ?: date('Y-m-d');
					$start_t = strtotime( $start_s );

					$earnings = $capital * ( $rate / 100.0 ) * ( $days / 365.25 );
					$maturity_val = $capital + $earnings;

					// Compute active progressive statistics
					$span_sec = $days * 86400;
					$elapsed  = time() - $start_t;
					$ratio    = $span_sec > 0 ? clamp_float( $elapsed / $span_sec, 0.0, 1.0 ) : 1.0;
					$percent  = round( $ratio * 100 );
					$remaining_days = max(0, ceil(($start_t + $span_sec - time()) / 86400));
				?>
					<article class="bg-slate-900 border border-gray-800 rounded-2xl p-6 hover:border-gray-700 transition">
						<header class="flex flex-wrap items-center justify-between gap-4 mb-4">
							<div>
								<h4 class="text-lg font-bold text-slate-100 hover:text-green-400 transition">
									<a href="<?php the_permalink(); ?>"><?php the_title(); ?></a>
								</h4>
								<span class="text-xs text-gray-500">Subscription Date: <?php echo esc_html($start_s); ?></span>
							</div>

							<div class="bg-slate-800 px-3 py-1.5 rounded-xl text-xs font-bold text-indigo-400 border border-gray-700">
								<?php echo esc_html($days); ?> Days Lockup
							</div>
						</header>

						<!-- Dynamic calculations list -->
						<div class="grid grid-cols-2 md:grid-cols-4 gap-4 p-4 bg-slate-950 rounded-xl mb-4 border border-gray-900">
							<div>
								<span class="text-10px text-gray-500 uppercase block">Principal capital</span>
								<strong class="text-sm font-extrabold text-slate-200"><?php echo number_format($capital); ?> FCFA</strong>
							</div>
							<div>
								<span class="text-10px text-gray-500 uppercase block">Interest Rate</span>
								<strong class="text-sm font-extrabold text-green-400"><?php echo esc_html($rate); ?>% APR</strong>
							</div>
							<div>
								<span class="text-10px text-gray-500 uppercase block">Potential Return</span>
								<strong class="text-sm font-extrabold text-slate-100"><?php echo number_format($maturity_val); ?> FCFA</strong>
							</div>
							<div>
								<span class="text-10px text-gray-500 uppercase block">Time Remaining</span>
								<strong class="text-sm font-extrabold text-indigo-400"><?php echo esc_html($remaining_days); ?> Days left</strong>
							</div>
						</div>

						<!-- Linear Progress Bar -->
						<div class="space-y-2">
							<div class="flex justify-between items-center text-xs">
								<span class="text-gray-400 font-bold">Lockup progress timeline</span>
								<span class="text-green-400 font-black"><?php echo esc_html($percent); ?>%</span>
							</div>
							<div class="w-full bg-slate-950 rounded-full h-3 p-0.5 border border-gray-800">
								<div class="bg-gradient-to-r from-green-500 via-emerald-400 to-indigo-500 h-2 rounded-full transition-all duration-500" style="width: <?php echo esc_attr($percent); ?>%; background-color:#10B981;"></div>
							</div>
						</div>
					</article>
				<?php endwhile; ?>
			<?php else : ?>
				<div class="bg-slate-900 border border-gray-800 rounded-2xl p-10 text-center">
					<p class="text-gray-400 text-sm">No investment active lockup posts discovered inside database. Tap seeded defaults above or insert custom posts.</p>
				</div>
			<?php endif; ?>

		</div>

		<!-- Right: Quick Live WordPress Theme configuration handbook -->
		<div class="space-y-6">
			
			<div id="how-to-use" class="bg-slate-900 border border-gray-800 rounded-3xl p-6 shadow-xl">
				<h3 class="text-lg font-bold text-slate-100 mb-4 flex items-center space-x-2">
					<span>🛠️ THEME INTEGRATION GUIDE</span>
				</h3>
				<p class="text-xs text-gray-400 leading-relaxed mb-4">
					This theme is fully compatible with standard self-hosted WordPress installations. Follow these 3 quick setup steps to go live:
				</p>

				<ol class="text-xs text-gray-300 space-y-4 list-decimal list-inside pl-1">
					<li>
						<strong>Export & Zip:</strong> Zip all files stored inside the <code>/wordpress-theme/</code> workspace directory (including <code>style.css</code>, <code>functions.php</code>, <code>index.php</code>, <code>header.php</code>, and <code>footer.php</code>).
					</li>
					<li>
						<strong>Install Panel:</strong> Go back to your WordPress Administration, select <em>Appearance</em> &rarr; <em>Themes</em> &rarr; <em>Add New</em>. Upload and active your zipped package.
					</li>
					<li>
						<strong>Manage Lockups:</strong> A specialized post-type named <strong>Invexa Lockups</strong> will appear in the admin column. Add custom post entries with numeric values inside custom fields to render on your modern visual web front-end!
					</li>
				</ol>

				<div class="bg-slate-950 p-4 rounded-xl border border-gray-900 mt-6 space-y-2">
					<span class="text-10px font-extrabold uppercase text-gray-500 block">Required Custom fields schema</span>
					<div class="grid grid-cols-2 gap-2 text-10px text-gray-400">
						<div>🏷️ <code>capital_locked</code></div>
						<div>numeric (e.g. 500000)</div>
						<div>🏷️ <code>interest_rate</code></div>
						<div>decimal % (e.g. 14.5)</div>
						<div>🏷️ <code>duration_days</code></div>
						<div>integer (e.g. 90)</div>
						<div>🏷️ <code>start_date</code></div>
						<div>ISO format (YYYY-MM-DD)</div>
					</div>
				</div>
			</div>

		</div>

	</div>

</main>

<?php
get_footer();
