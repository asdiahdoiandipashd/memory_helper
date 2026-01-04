package com.example.memoryhelper.di

import android.content.Context
import androidx.room.Room
import com.example.memoryhelper.data.local.AppDatabase
import com.example.memoryhelper.data.local.dao.MemoryItemDao
import com.example.memoryhelper.data.local.dao.ReviewCurveDao
import com.example.memoryhelper.data.local.dao.ReviewLogDao
import com.example.memoryhelper.data.local.dao.TodoTagDao
import com.example.memoryhelper.data.local.dao.TodoTaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Module for providing Database and DAO instances.
 */
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
            // 移除 fallbackToDestructiveMigration，保护用户数据
            // 如果需要迁移，应该添加具体的 Migration 策略
            // .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .addMigrations(AppDatabase.MIGRATION_3_4)
            .build()
    }

    @Provides
    @Singleton
    fun provideReviewCurveDao(database: AppDatabase): ReviewCurveDao {
        return database.reviewCurveDao()
    }

    @Provides
    @Singleton
    fun provideMemoryItemDao(database: AppDatabase): MemoryItemDao {
        return database.memoryItemDao()
    }

    @Provides
    @Singleton
    fun provideReviewLogDao(database: AppDatabase): ReviewLogDao {
        return database.reviewLogDao()
    }

    @Provides
    @Singleton
    fun provideNotebookDao(database: AppDatabase): com.example.memoryhelper.data.local.dao.NotebookDao {
        return database.notebookDao()
    }

    @Provides
    @Singleton
    fun provideTodoTaskDao(database: AppDatabase): TodoTaskDao {
        return database.todoTaskDao()
    }

    @Provides
    @Singleton
    fun provideTodoTagDao(database: AppDatabase): TodoTagDao {
        return database.todoTagDao()
    }
}
