package com.shiori.domain.usecase

interface SuspendUseCase<in Params, out T> {
    fun execute(params: Params) : T
}