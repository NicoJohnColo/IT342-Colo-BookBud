package com.example.bookbud.shared.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<T : ViewBinding> : AppCompatActivity() {
    protected lateinit var binding: T
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = getViewBinding()
        setContentView(binding.root)
        setupUI()
        setupObservers()
    }
    
    abstract fun getViewBinding(): T
    open fun setupUI() {}
    open fun setupObservers() {}
}
