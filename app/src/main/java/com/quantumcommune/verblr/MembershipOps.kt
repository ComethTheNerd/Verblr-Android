package com.quantumcommune.verblr

class MembershipOps {

    val FLAG_admin = 0x1;
    val FLAG_disabled = 0x2;
    val FLAG_unlimited = 0x4;
    val FLAG_debug = 0x8

    val TIER_anon = "anon"
    val TIER_standard = "standard"
    val TIER_premium = "premium"
    val TIER_ulta = "ultra"

    fun isNullOrAnon(membership: DATA_UserMembership?) = membership == null || isAnon(membership)

    fun isAnon(membership: DATA_UserMembership?) : Boolean
    {
        return membership?.tier == TIER_anon
    }

    fun isAdmin(membership: DATA_UserMembership?) : Boolean
    {
        if(membership != null)
        {
            return (membership.flags and FLAG_admin) == FLAG_admin
        }

        return false
    }

    fun isPaid(membership: DATA_UserMembership?) : Boolean
    {
        if(membership != null)
        {
            val tier = membership.tier

            return tier == TIER_premium || tier == TIER_ulta
        }

        return false
    }

    fun isLimited(membership: DATA_UserMembership?) : Boolean
    {
        if(isAdmin(membership))
        {
            return false
        }

        if(membership != null)
        {
            if((membership.flags and FLAG_unlimited) == FLAG_unlimited)
            {
                return false
            }

            return membership.credits.count() > 0
        }

        return true
    }

    fun useOfflineCaching(membership: DATA_UserMembership?) : Boolean
    {
        return isPaid(membership);
    }
}