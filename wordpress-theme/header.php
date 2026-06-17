<!DOCTYPE html>
<html <?php language_attributes(); ?>>
<head>
	<meta charset="<?php bloginfo( 'charset' ); ?>">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<?php wp_head(); ?>
	<style>
		body {
			font-family: 'Plus Jakarta Sans', sans-serif;
			background-color: #0F172A; /* Slate Theme Background */
			color: #F8FAFC;
		}
		h1, h2, h3, h4 {
			font-family: 'Space Grotesk', sans-serif;
		}
	</style>
</head>
<body <?php body_class( 'antialiased min-h-screen flex flex-col' ); ?>>

<header class="border-b border-gray-800 bg-slate-900 shadow-xl sticky top-0 z-50">
	<div class="max-width container mx-auto px-6 py-4 flex items-center justify-between">
		
		<!-- Brand Identity -->
		<div class="flex items-center space-x-3">
			<div class="h-10 w-10 rounded-xl bg-gradient-to-tr from-green-400 to-green-600 flex items-center justify-center shadow-lg transform hover:scale-105 transition">
				<span class="text-white font-extrabold text-xl tracking-tight">I</span>
			</div>
			<div>
				<h1 class="text-lg font-bold text-slate-100 tracking-wide uppercase">Invexa</h1>
				<p class="text-xs text-green-400 font-bold tracking-widest mt-0.5">FINANCIAL WEB COMPANION</p>
			</div>
		</div>

		<!-- Simulated Wallet stats & indicators directly linked to primary ledger -->
		<div class="hidden md:flex items-center space-x-6">
			<div class="bg-slate-800 px-4 py-2 rounded-xl border border-gray-700 flex items-center space-x-3">
				<div class="p-1.5 rounded-lg bg-green-500 bg-opacity-20 text-green-400">
					<!-- Wallet SVG icon -->
					<svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
						<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
					</svg>
				</div>
				<div>
					<span class="block text-10px text-gray-400 uppercase tracking-wider leading-none">Wallet balance</span>
					<span class="text-sm font-black text-slate-100">8,540,200 FCFA</span>
				</div>
			</div>

			<div class="bg-slate-800 px-4 py-2 rounded-xl border border-gray-700 flex items-center space-x-3">
				<div class="p-1.5 rounded-lg bg-indigo-500 bg-opacity-20 text-indigo-400">
					<!-- Shield/Check Icon -->
					<svg class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
						<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
					</svg>
				</div>
				<div>
					<span class="block text-10px text-gray-400 uppercase tracking-wider leading-none">KYC tier status</span>
					<span class="text-sm font-black text-green-400">TIER-3 APPROVED</span>
				</div>
			</div>
		</div>

		<!-- Link Back to Android Emulator Info -->
		<a href="<?php echo esc_url( home_url('/') ); ?>" class="bg-green-500 hover:bg-green-600 text-white font-extrabold text-xs px-4 py-2.5 rounded-xl uppercase tracking-wider transition-all shadow-md hover:shadow-green-500/20">
			Refresh Feed
		</a>
	</div>
</header>
