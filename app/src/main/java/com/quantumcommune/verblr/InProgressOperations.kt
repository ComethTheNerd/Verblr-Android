package com.quantumcommune.verblr

import com.quantumcommune.verblr.ui.main.VRBViewModel

class InProgressOperations(val viewModel: VRBViewModel) {
     companion object
     {
         val FLAG_initialize = 0x1;
         val FLAG_addArticle = 0x2;
         val FLAG_purchase = 0x4;
         val FLAG_authenticate = 0x8;
         val FLAG_fetchBundle = 0x10;
         val FLAG_fetchLibrary = 0x20;
         val FLAG_fetchMembership = 0x40;
         val FLAG_maximizePlayer = 0x80;
         val FLAG_accessPerformance = 0x100;
         val FLAG_updatePerformanceProgress = 0x200;
         val FLAG_libraryUpdate = 0x400;
         val FLAG_inviteByEmail = 0x800;
         val FLAG_processAddArticleParamsQueue = 0x1000;

         val MASK_switchUser = FLAG_initialize or FLAG_authenticate
     }

    fun hasFlags(flags : Int) : Boolean
    {
        return ((viewModel.ipo.value ?: 0) and flags) == flags
    }

    fun addFlags(flags : Int)
    {
        val newValue = (viewModel.ipo.value ?: 0) or flags;

        if(ThreadUtils.isMainThread())
        {
            viewModel.ipo.value = newValue
        }
        else
        {
            viewModel.ipo.postValue(newValue)
        }
    }

    fun removeFlags(flags: Int)
    {
        val newValue = (viewModel.ipo.value ?: 0) and flags.inv();

        if(ThreadUtils.isMainThread())
        {
            viewModel.ipo.value = newValue
        }
        else
        {
            viewModel.ipo.postValue(newValue)
        }
    }
}