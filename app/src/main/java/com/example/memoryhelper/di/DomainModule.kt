package com.example.memoryhelper.di

import com.example.memoryhelper.data.importing.DefaultImportService
import com.example.memoryhelper.data.sync.LocalFirstSyncService
import com.example.memoryhelper.domain.importing.ImportService
import com.example.memoryhelper.domain.plan.DefaultPlanEngine
import com.example.memoryhelper.domain.plan.PlanEngine
import com.example.memoryhelper.domain.scheduler.DefaultSchedulerEngine
import com.example.memoryhelper.domain.scheduler.SchedulerEngine
import com.example.memoryhelper.domain.sync.SyncService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DomainModule {

    @Binds
    @Singleton
    abstract fun bindSchedulerEngine(impl: DefaultSchedulerEngine): SchedulerEngine

    @Binds
    @Singleton
    abstract fun bindPlanEngine(impl: DefaultPlanEngine): PlanEngine

    @Binds
    @Singleton
    abstract fun bindImportService(impl: DefaultImportService): ImportService

    @Binds
    @Singleton
    abstract fun bindSyncService(impl: LocalFirstSyncService): SyncService
}
