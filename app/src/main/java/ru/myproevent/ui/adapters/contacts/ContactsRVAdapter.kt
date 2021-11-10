package ru.myproevent.ui.adapters.contacts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import ru.myproevent.R
import ru.myproevent.databinding.ItemContactBinding
import ru.myproevent.domain.model.entities.Status
import ru.myproevent.ui.presenters.contacts.IContactItemView
import ru.myproevent.utils.load


class ContactsRVAdapter(val presenter: IContactsListPresenter) :
    RecyclerView.Adapter<ContactsRVAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = presenter.getCount()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        presenter.bindView(holder.apply { pos = position })

    override fun onViewRecycled(holder: ViewHolder) = holder.unbind()

    inner class ViewHolder(private val vb: ItemContactBinding) : RecyclerView.ViewHolder(vb.root),
        IContactItemView {

        init {
            itemView.setOnClickListener { presenter.onItemClick(this) }
            vb.requestStatus.setOnClickListener { (presenter.onStatusClick(this)) }
            vb.requestStatusHitArea.setOnClickListener { vb.requestStatus.performClick() }
        }

        override fun setName(name: String) {
            vb.tvName.text = name
        }

        override fun setDescription(description: String) {
            vb.tvDescription.text = description
        }

        override fun loadImg(url: String) {
            vb.ivImg.load(url)
        }

        override fun setStatus(status: Status) = with(vb) {
            requestStatus.setImageDrawable(
                when (status) {
                    Status.REQUESTED -> AppCompatResources.getDrawable(
                        itemView.context,
                        R.drawable.ic_incomming_request
                    )
                    Status.DECLINED -> AppCompatResources.getDrawable(
                        itemView.context,
                        R.drawable.ic_rejected_request
                    )
                    Status.PENDING -> AppCompatResources.getDrawable(
                        itemView.context,
                        R.drawable.ic_outgoing_request
                    )
                    else -> null
                }
            )
        }

        override var pos = -1

        fun unbind() = with(vb) {
            // TODO: не понял зачем это
//            ivImg.setImageDrawable(null)
//            requestStatus.setImageDrawable(null)
        }
    }
}