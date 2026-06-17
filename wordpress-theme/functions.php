<?php
/**
 * Invexa Financial Theme Functions and Definitions
 *
 * @package InvexaTheme
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit; // Exit if accessed directly.
}

function invexa_setup() {
	// Add support for core features
	add_theme_support( 'title-tag' );
	add_theme_support( 'post-thumbnails' );
	add_theme_support( 'html5', array( 'search-form', 'comment-form', 'comment-list', 'gallery', 'caption', 'style', 'script' ) );
}
add_action( 'after_setup_theme', 'invexa_setup' );

// Register styles and scripts
function invexa_enqueue_assets() {
	// Core stylesheet
	wp_enqueue_style( 'invexa-style', get_stylesheet_uri(), array(), '1.0.0' );

	// Fetch dynamic responsive styling framework (Tailwind CSS CDN for quick setup)
	wp_enqueue_style( 'tailwindcss', 'https://cdn.jsdelivr.net/npm/tailwindcss@2.2.19/dist/tailwind.min.css', array(), '2.2.19' );

	// Font pairing for Space Grotesk / Inter feel
	wp_enqueue_style( 'google-fonts', 'https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@300;400;500;600;700;800&family=Space+Grotesk:wght@500;700&display=swap', array(), '1.0' );
}
add_action( 'wp_enqueue_scripts', 'invexa_enqueue_assets' );

// Register custom post type 'Investment Lockups' to mimic SQLite entity models fully
function invexa_register_lockup_post_type() {
	$labels = array(
		'name'               => _x( 'Investment Lockups', 'post type general name', 'invexa-theme' ),
		'singular_name'      => _x( 'Investment Lockup', 'post type singular name', 'invexa-theme' ),
		'menu_name'          => _x( 'Invexa Lockups', 'admin menu', 'invexa-theme' ),
		'add_new_item'       => __( 'Add New Active Lockup', 'invexa-theme' ),
		'edit_item'          => __( 'Edit Lockup Metrics', 'invexa-theme' ),
		'all_items'          => __( 'All Investment Lockups', 'invexa-theme' ),
		'view_item'          => __( 'View Dynamic Progress', 'invexa-theme' ),
		'search_items'       => __( 'Search Lockups', 'invexa-theme' ),
		'not_found'          => __( 'No active financial lockups found.', 'invexa-theme' ),
	);

	$args = array(
		'labels'             => $labels,
		'public'             => true,
		'has_archive'        => true,
		'menu_icon'          => 'dashicons-chart-area', // Admin chart layout icon
		'supports'           => array( 'title', 'editor', 'custom-fields' ),
		'show_in_rest'       => true,
	);

	register_post_type( 'investment_lockup', $args );
}
add_action( 'init', 'invexa_register_lockup_post_type' );

// Seed default lockups for a ready-to-run showcase if none exist
function invexa_seed_mock_data_if_empty() {
	if ( get_transient( 'invexa_mock_data_seeded' ) ) {
		return;
	}

	$existing = get_posts( array( 'post_type' => 'investment_lockup' ) );
	if ( empty( $existing ) ) {
		$seeds = array(
			array(
				'title'          => 'Cameroon Sovereign Agri-Bond',
				'capital'        => 500000,
				'rate'           => 14.5,
				'duration_days'  => 30,
				'start_date'     => date( 'Y-m-d', strtotime( '-12 days' ) ),
			),
			array(
				'title'          => 'Invexa Micro-Cap High-Yield Pool',
				'capital'        => 250000,
				'rate'           => 22.0,
				'duration_days'  => 60,
				'start_date'     => date( 'Y-m-d', strtotime( '-8 days' ) ),
			),
			array(
				'title'          => 'Bassa Community Real-Estate Trust',
				'capital'        => 1200000,
				'rate'           => 8.7,
				'duration_days'  => 90,
				'start_date'     => date( 'Y-m-d', strtotime( '-45 days' ) ),
			)
		);

		foreach ( $seeds as $seed ) {
			$post_id = wp_insert_post( array(
				'post_title'   => $seed['title'],
				'post_status'  => 'publish',
				'post_type'    => 'investment_lockup',
			) );

			if ( $post_id ) {
				update_post_meta( $post_id, 'capital_locked', $seed['capital'] );
				update_post_meta( $post_id, 'interest_rate', $seed['rate'] );
				update_post_meta( $post_id, 'duration_days', $seed['duration_days'] );
				update_post_meta( $post_id, 'start_date', $seed['start_date'] );
			}
		}
	}

	set_transient( 'invexa_mock_data_seeded', true, WEEK_IN_SECONDS );
}
add_action( 'wp_loaded', 'invexa_seed_mock_data_if_empty' );
