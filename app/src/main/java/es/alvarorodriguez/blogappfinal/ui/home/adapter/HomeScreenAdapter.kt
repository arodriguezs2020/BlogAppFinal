package es.alvarorodriguez.blogappfinal.ui.home.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import es.alvarorodriguez.blogappfinal.R
import es.alvarorodriguez.blogappfinal.core.BaseViewHolder
import es.alvarorodriguez.blogappfinal.core.TimeUtils
import es.alvarorodriguez.blogappfinal.core.hide
import es.alvarorodriguez.blogappfinal.core.show
import es.alvarorodriguez.blogappfinal.data.model.Post
import es.alvarorodriguez.blogappfinal.databinding.PostItemViewBinding

class HomeScreenAdapter(private val postList: List<Post>, private val onPostClickListener: OnPostClickListener): RecyclerView.Adapter<BaseViewHolder<*>>() {

    private var postClickListener: OnPostClickListener? = null

    init {
        postClickListener = onPostClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        val itemBinding = PostItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HomeScreenViewHolder(itemBinding, parent.context)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        when(holder) {
            is HomeScreenViewHolder -> holder.bind(postList[position])
        }
    }

    override fun getItemCount(): Int = postList.size

    // Aqui creamos el ViewHolder
    private inner class HomeScreenViewHolder(
        val binding: PostItemViewBinding,
        val context: Context
    ) : BaseViewHolder<Post>(binding.root) {
        override fun bind(item: Post) {
            setupProfileInfo(item)
            addPostTimeStamp(item)
            setupPostImage(item)
            setupPostDescription(item)
            tintHeartIcon(item)
            setupLikeCount(item)
            setLikeClickAction(item)
        }

        // Con este metodo creamos el perfil del usuario
        private fun setupProfileInfo(post: Post) {
            Glide.with(context).load(post.poster?.profile_picture).centerCrop().into(binding.profilePicture)
            binding.profileName.text = post.poster?.username
        }

        // Con este metodo añadimos el tiempo que lleva creado el post
        private fun addPostTimeStamp(post: Post) {
            val created_at = (post.created_at?.time?.div(1000L))?.let {
                TimeUtils.getTimeAgo(it.toInt())
            }
            binding.postTimestamp.text = created_at
        }

        // Con este metodo añadimos la imagen del post
        private fun setupPostImage(post: Post) {
            Glide.with(context).load(post.post_image).centerCrop().into(binding.postImage)
        }

        // Con este metodo añadimos la descripcion del post
        private fun setupPostDescription(post: Post) {
            if(post.post_description.isEmpty()){
                binding.postDescription.visibility = View.GONE
            } else {
                binding.postDescription.text = post.post_description
            }
        }

        // Con este metodo cambiamos los colores del like dependiendo si liked este en false o true
        private fun tintHeartIcon(post: Post) {
            if (!post.liked) {
                binding.likeBtn.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_baseline_favorite_border_24))
                binding.likeBtn.setColorFilter(ContextCompat.getColor(context, R.color.black))
            } else {
                binding.likeBtn.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_baseline_favorite_24))
                binding.likeBtn.setColorFilter(ContextCompat.getColor(context, R.color.red_like))
            }
        }

        @SuppressLint("SetTextI18n")
        private fun setupLikeCount(post: Post) {
            if (post.likes > 0) {
                binding.likeCount.show()
                binding.likeCount.text = "${post.likes} likes"
            } else {
                binding.likeCount.hide()
            }
        }

        // Y con este lo que hacemos es que cuando le demos al boton de like cambiamos el estado de liked
        private fun setLikeClickAction(post: Post) {
            binding.likeBtn.setOnClickListener {
                if (post.liked) {
                    post.apply {
                        liked = false
                    }
                } else {
                    post.apply {
                        liked = true
                    }
                }
                tintHeartIcon(post)
                postClickListener?.onLikeButtonClick(post, post.liked)
            }
        }
    }
}

interface OnPostClickListener {
    fun onLikeButtonClick(post: Post, liked: Boolean)
}