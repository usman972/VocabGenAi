
package com.rameez.hel.network

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {
    
    // IMPORTANT: Replace these with your actual Bolt Database credentials
    private const val SUPABASE_URL = "YOUR_SUPABASE_URL_HERE"
    private const val SUPABASE_KEY = "YOUR_SUPABASE_ANON_KEY_HERE"
    
    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Postgrest)
    }
}
