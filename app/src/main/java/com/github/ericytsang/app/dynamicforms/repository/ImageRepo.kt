package com.github.ericytsang.app.dynamicforms.repository

import com.github.ericytsang.app.dynamicforms.database.AppDatabase

class ImageRepo(
    private val db:AppDatabase,
    private val imageDownloader:ImageDownloader
)
{
    private val imageDao = db.imageDao()

    fun getImageBytes(imageUrl:Url)
    {
/*
        db.runInTransaction()
        {
            val imagePk = ImageEntity.Pk(imageUrl.url)
            val image = imageDao.selectOne(imagePk)

            // if we've started / completed downloading the image before, then
            // use it
            if (image != null)
            {}

            // start downloading the image otherwise
            else
            {
                imageDao.insert(ImageEntity(imagePk,ImageEntity.Values()))
            }
        }
*/
    }
}