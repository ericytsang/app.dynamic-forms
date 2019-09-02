package com.github.ericytsang.app.dynamicforms.database

import androidx.annotation.VisibleForTesting
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

// todo: maybe i can move all DAOs and tests into it's own library module to isolate its test-only functions more

@Dao
abstract class FormFieldDao
{
    @Insert(entity = FormFieldEntity::class,onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertOrUpdate(formField:FormFieldEntity)

    @Insert(entity = FormFieldEntity::class)
    protected abstract fun _create(values:FormFieldEntity.Values):Long
    fun create(values:FormFieldEntity.Values) = FormFieldEntity.Pk(_create(values))

    // todo: add @VisibleForTesting for unused DAO methods on other DAOs
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    @Query("SELECT * FROM FormFieldEntity")
    abstract fun selectAll():List<FormFieldEntity>

    @Query("SELECT * FROM FormFieldEntity WHERE formId = :formId ORDER BY positionInForm")
    protected abstract fun _selectAllForForm(formId:Long):List<FormFieldEntity>
    fun selectAllForForm(formPk:FormEntity.Pk) = _selectAllForForm(formPk.id)

    @Query("SELECT * FROM FormFieldEntity WHERE id = :id")
    protected abstract fun _selectOne(id:Long):FormFieldEntity?
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun selectOne(pk:FormFieldEntity.Pk) = _selectOne(pk.id)

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    @Update(entity = FormFieldEntity::class,onConflict = OnConflictStrategy.ABORT)
    abstract fun update(form:FormFieldEntity)

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    @Delete(entity = FormFieldEntity::class)
    abstract fun delete(pk:FormFieldEntity.Pk)
}

interface FormFieldSubclassDao<Subclass:FormFieldEntitySubclass>
{
    fun selectAllForForm(formPk:FormEntity.Pk):List<Subclass>
}

@Dao
abstract class TextFormFieldDao:FormFieldSubclassDao<TextFormFieldEntity>
{
    @Insert(entity = TextFormFieldEntity::class)
    abstract fun insert(values:TextFormFieldEntity)

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    @Query("SELECT * FROM TextFormFieldEntity")
    abstract fun selectAll():List<TextFormFieldEntity>

    @Query("""
        SELECT * FROM TextFormFieldEntity AS Child
            INNER JOIN FormFieldEntity AS Parent
                ON Parent.id = Child.id
            WHERE Parent.formId = :formId
    """)
    protected abstract fun _selectAllForForm(formId:Long):List<TextFormFieldEntity>
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    override fun selectAllForForm(formPk:FormEntity.Pk) = _selectAllForForm(formPk.id)

    @Query("SELECT * FROM TextFormFieldEntity WHERE id = :id")
    protected abstract fun _selectOne(id:Long):TextFormFieldEntity?
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun selectOne(pk:FormFieldEntity.Pk) = _selectOne(pk.id)

    @Update(entity = TextFormFieldEntity::class,onConflict = OnConflictStrategy.ABORT)
    abstract fun update(form:TextFormFieldEntity)

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    @Delete(entity = TextFormFieldEntity::class)
    abstract fun delete(pk:FormFieldEntity.Pk)
}

@Dao
abstract class DateFormFieldDao:FormFieldSubclassDao<DateFormFieldEntity>
{
    @Insert(entity = DateFormFieldEntity::class)
    abstract fun insert(values:DateFormFieldEntity)

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    @Query("SELECT * FROM DateFormFieldEntity")
    abstract fun selectAll():List<DateFormFieldEntity>

    @Query("""
        SELECT * FROM DateFormFieldEntity AS Child
            INNER JOIN FormFieldEntity AS Parent
                ON Parent.id = Child.id
            WHERE Parent.formId = :formId
    """)
    protected abstract fun _selectAllForForm(formId:Long):List<DateFormFieldEntity>
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    override fun selectAllForForm(formPk:FormEntity.Pk) = _selectAllForForm(formPk.id)

    @Query("SELECT * FROM DateFormFieldEntity WHERE id = :id")
    protected abstract fun _selectOne(id:Long):DateFormFieldEntity?
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun selectOne(pk:FormFieldEntity.Pk) = _selectOne(pk.id)

    @Update(entity = DateFormFieldEntity::class,onConflict = OnConflictStrategy.ABORT)
    abstract fun update(form:DateFormFieldEntity)

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    @Delete(entity = DateFormFieldEntity::class)
    abstract fun delete(pk:FormFieldEntity.Pk)
}
