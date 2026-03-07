package com.example.memoryhelper.di

import android.content.Context
import androidx.room.Room
import com.example.memoryhelper.data.local.AppDatabase
import com.example.memoryhelper.data.local.dao.DailyPlanItemDao
import com.example.memoryhelper.data.local.dao.ExamPlanDao
import com.example.memoryhelper.data.local.dao.ExamSubjectDao
import com.example.memoryhelper.data.local.dao.MemoryItemDao
import com.example.memoryhelper.data.local.dao.MemoryItemTagDao
import com.example.memoryhelper.data.local.dao.MemoryTagDao
import com.example.memoryhelper.data.local.dao.NotebookDao
import com.example.memoryhelper.data.local.dao.ReviewCurveDao
import com.example.memoryhelper.data.local.dao.ReviewLogDao
import com.example.memoryhelper.data.local.dao.SyncEventDao
import com.example.memoryhelper.data.local.dao.SyncStateDao
import com.example.memoryhelper.data.local.dao.TodoTagDao
import com.example.memoryhelper.data.local.dao.TodoTaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .addMigrations(
                AppDatabase.MIGRATION_3_4,
                AppDatabase.MIGRATION_4_5,
                AppDatabase.MIGRATION_5_6,
                AppDatabase.MIGRATION_6_7
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideReviewCurveDao(database: AppDatabase): ReviewCurveDao = database.reviewCurveDao()

    @Provides
    @Singleton
    fun provideMemoryItemDao(database: AppDatabase): MemoryItemDao = database.memoryItemDao()

    @Provides
    @Singleton
    fun provideReviewLogDao(database: AppDatabase): ReviewLogDao = database.reviewLogDao()

    @Provides
    @Singleton
    fun provideNotebookDao(database: AppDatabase): NotebookDao = database.notebookDao()

    @Provides
    @Singleton
    fun provideTodoTaskDao(database: AppDatabase): TodoTaskDao = database.todoTaskDao()

    @Provides
    @Singleton
    fun provideTodoTagDao(database: AppDatabase): TodoTagDao = database.todoTagDao()

    @Provides
    @Singleton
    fun provideExamPlanDao(database: AppDatabase): ExamPlanDao = database.examPlanDao()

    @Provides
    @Singleton
    fun provideExamSubjectDao(database: AppDatabase): ExamSubjectDao = database.examSubjectDao()

    @Provides
    @Singleton
    fun provideDailyPlanItemDao(database: AppDatabase): DailyPlanItemDao = database.dailyPlanItemDao()

    @Provides
    @Singleton
    fun provideMemoryTagDao(database: AppDatabase): MemoryTagDao = database.memoryTagDao()

    @Provides
    @Singleton
    fun provideMemoryItemTagDao(database: AppDatabase): MemoryItemTagDao = database.memoryItemTagDao()

    @Provides
    @Singleton
    fun provideSyncEventDao(database: AppDatabase): SyncEventDao = database.syncEventDao()

    @Provides
    @Singleton
    fun provideSyncStateDao(database: AppDatabase): SyncStateDao = database.syncStateDao()
}
