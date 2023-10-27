package com.desarrollodroide.domain.usecase

interface SuspendUseCase<in Params, out T> {
    fun execute(params: Params) : T
}