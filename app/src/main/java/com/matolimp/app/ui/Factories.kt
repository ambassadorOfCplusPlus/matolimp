package com.matolimp.app.ui

import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.matolimp.app.data.Repository

fun courseVmFactory(repo: Repository) = viewModelFactory {
    initializer { CourseViewModel(repo) }
}

fun themeVmFactory(repo: Repository, themeId: String) = viewModelFactory {
    initializer { ThemeViewModel(repo, themeId) }
}

fun razborVmFactory(repo: Repository) = viewModelFactory {
    initializer { RazborViewModel(repo) }
}

fun problemVmFactory(repo: Repository, problemId: String) = viewModelFactory {
    initializer { ProblemViewModel(repo, problemId) }
}
