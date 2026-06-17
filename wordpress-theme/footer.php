<?php
/**
 * Invexa Financial Theme Footer
 *
 * @package InvexaTheme
 */
?>

<footer class="mt-auto border-t border-gray-800 bg-slate-950 py-10">
	<div class="max-width container mx-auto px-6 grid grid-cols-1 md:grid-cols-3 gap-8">
		
		<div>
			<div class="flex items-center space-x-2">
				<div class="h-6 w-6 rounded bg-green-500 flex items-center justify-center font-bold text-white text-xs">I</div>
				<span class="text-slate-200 font-extrabold uppercase text-sm tracking-widest">Invexa Web Core</span>
			</div>
			<p class="text-xs text-gray-400 mt-4 leading-relaxed">
				Standardized decentralised savings platform for High-Yield sovereign micro-assets in Central Africa. Persists on secure Room DB engines and connects to cloud networks.
			</p>
		</div>

		<div>
			<h4 class="text-xs font-bold text-slate-100 uppercase tracking-widest">Sovereign Compliance</h4>
			<p class="text-xs text-gray-500 mt-4 leading-relaxed">
				All active microfinance portfolios and sovereign products are curated under regulatory policies in Yaoundé, Cameroon. Guaranteed lockup conditions apply.
			</p>
		</div>

		<div>
			<h4 class="text-xs font-bold text-slate-100 uppercase tracking-widest">System Identity</h4>
			<ul class="text-xs text-gray-400 mt-4 space-y-2">
				<li>Local standard: <strong>Cameroon (XAF/FCFA)</strong></li>
				<li>Gateway channels: <strong>MTN MoMo, Orange Money</strong></li>
				<li>Reconciliations: <strong>Retrofit & OKHttp Secure Web Gateways</strong></li>
			</ul>
		</div>

	</div>

	<div class="text-center mt-12 pt-6 border-t border-gray-900">
		<p class="text-xs text-gray-600">
			&copy; <?php echo date('Y'); ?> Invexa Cameroon. Styled dynamically on Cosmic Slate Visual System.
		</p>
	</div>
</footer>

<?php wp_footer(); ?>
</body>
</html>
