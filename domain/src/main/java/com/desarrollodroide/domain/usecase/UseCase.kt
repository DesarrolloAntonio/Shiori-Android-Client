package com.desarrollodroide.domain.usecase

interface UseCase<in Params, out T> {
    fun execute(params: Params): T
}