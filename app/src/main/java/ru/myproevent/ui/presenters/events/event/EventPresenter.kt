package ru.myproevent.ui.presenters.events.event

import android.util.Log
import android.widget.Toast
import com.github.terrakok.cicerone.Router
import ru.myproevent.ProEventApp
import ru.myproevent.R
import ru.myproevent.domain.models.entities.Address
import ru.myproevent.domain.models.entities.Event
import ru.myproevent.domain.models.entities.Profile
import ru.myproevent.domain.models.entities.TimeInterval
import ru.myproevent.domain.models.repositories.events.IProEventEventsRepository
import ru.myproevent.domain.models.repositories.images.IImagesRepository
import ru.myproevent.domain.models.repositories.proevent_login.IProEventLoginRepository
import ru.myproevent.domain.models.repositories.profiles.IProEventProfilesRepository
import ru.myproevent.domain.utils.toParticipantItems
import ru.myproevent.ui.adapters.event_items.*
import ru.myproevent.ui.adapters.event_items.presenter_interfaces.IEventScreenListPresenter
import ru.myproevent.ui.adapters.event_items.presenter_interfaces.IFormsHeaderItemPresenter
import ru.myproevent.ui.adapters.IItemPresenter
import ru.myproevent.ui.adapters.event_items.presenter_interfaces.IEventDateItemPresenter
import ru.myproevent.ui.adapters.event_items.view_item_interfaces.*
import ru.myproevent.ui.presenters.BaseMvpPresenter
import java.io.File
import java.lang.RuntimeException
import java.util.*
import javax.inject.Inject

class EventPresenter(localRouter: Router, var eventBeforeEdit: Event?) :
    BaseMvpPresenter<EventView>(localRouter) {

    @Inject
    lateinit var eventsRepository: IProEventEventsRepository

    @Inject
    lateinit var loginRepository: IProEventLoginRepository

    @Inject
    lateinit var profilesRepository: IProEventProfilesRepository

    @Inject
    lateinit var imagesRepository: IImagesRepository

    val participantProfiles = mutableMapOf<Long, Profile>()

//    private val dates: MutableList<TimeInterval>? = mutableListOf(
//        TimeInterval(1646817780000, 1646821380000),
//        TimeInterval(1643977614L * 1000L, (1643977614L + 3600L) * 1000L),
//        TimeInterval((1646815896L - 3600L * 2L) * 1000L, 1646815896L * 1000L),
//        TimeInterval(1646815896L * 1000L, (1646815896L + 3600L * 2L) * 1000L),
//        TimeInterval((1646815896L + 86400L * 2L) * 1000L, (1646815896L + 86400L * 3L) * 1000L)
//    ).apply {
//        sortWith { a, b ->
//            val longDiff = a.start - b.start
//            return@sortWith if (longDiff > 0L) {
//                1
//            } else {
//                -1
//            }
//        }
//    }

    private fun removeHeaderItemsFromScreenItems(headerPosition: Int) {
        eventScreenListPresenter.eventScreenItems.subList(
            headerPosition + 1,
            headerPosition + 1 + (eventScreenListPresenter.eventScreenItems[headerPosition] as EventScreenItem.FormsHeader<EventScreenItem.ListItem>).items.size
        ).clear()
    }

    private fun addHeaderItemsToScreenItems(
        headerPosition: Int,
        items: TreeSet<EventScreenItem.ListItem>
    ) {
        eventScreenListPresenter.eventScreenItems.addAll(headerPosition + 1, items)
    }

    private fun enableDescriptionEdit(headerPosition: Int) {
        with(eventScreenListPresenter.eventScreenItems[headerPosition] as EventScreenItem.FormsHeader<EventScreenItem.ListItem>) {
            if (items.first() is EventScreenItem.NoItemsPlaceholder) {
                removeHeaderItemsFromScreenItems(headerPosition)
                addHeaderItemsToScreenItems(headerPosition, items.apply {
                    clear()
                    add(
                        EventScreenItem.TextBox(
                            value = "",
                            header = this@with,
                            isEditLocked = false,
                            hasFocusIntent = true
                        )
                    )
                })
                viewState.updateEventScreenList()
            } else {
                if (!isExpanded) {
                    isExpanded = true
                    addHeaderItemsToScreenItems(headerPosition, items)
                }
                with(items.first() as EventScreenItem.TextBox) {
                    isEditLocked = false
                    hasFocusIntent = true
                }
                viewState.updateEventScreenList()
            }
        }
    }

    private class NoDescriptionPlaceholderFactory {
        companion object {
            fun create(header: EventScreenItem.FormsHeader<EventScreenItem.ListItem>) =
                EventScreenItem.NoItemsPlaceholder(
                    "Отсутствует.\nНажмите + чтобы добавить.",
                    R.drawable.ic_edit_blue,
                    21,
                    header
                )

        }
    }

    private class NoDatesPlaceholderFactory {
        companion object {
            fun create(header: EventScreenItem.FormsHeader<EventScreenItem.ListItem>) =
                EventScreenItem.NoItemsPlaceholder(
                    "Отсутствует.\nНажмите + чтобы добавить.",
                    R.drawable.ic_add,
                    21,
                    header
                )
        }
    }

    private class NoItemsPlaceholderFactory {
        companion object {
            fun create(header: EventScreenItem.FormsHeader<EventScreenItem.ListItem>) =
                EventScreenItem.NoItemsPlaceholder(
                    "Отсутствуют.\nНажмите + чтобы добавить.",
                    R.drawable.ic_add,
                    21,
                    header
                )
        }
    }

    private val setOfExpandedItems = mutableSetOf<EVENT_SCREEN_ITEM_ID>()

    private val isCurrentUserOwnsEvent by lazy {
        eventBeforeEdit?.let { loginRepository.getLocalId()!! == it.ownerUserId } ?: true
    }

    private fun getEventScreenItemsBeforeEdit(): MutableList<EventScreenItem> {
        return mutableListOf(
            EventScreenItem.ProfileImageForm(
                itemId = EVENT_SCREEN_ITEM_ID.EVENT_PICTURE,
                text = "Изменить фото мероприятия"
            ),
            EventScreenItem.TextForm(
                itemId = EVENT_SCREEN_ITEM_ID.EVENT_NAME,
                title = "Название",
                hint = "Введите название",
                value = eventBeforeEdit?.name ?: "",
                isEditLocked = true,
                isEditOptionAvailable = isCurrentUserOwnsEvent
            ),
            EventScreenItem.TextForm(
                itemId = EVENT_SCREEN_ITEM_ID.LOCATION,
                title = "Место проведения",
                hint = "Введите адрес",
                value = eventBeforeEdit?.address?.toString() ?: "",
                isEditLocked = true,
                isEditOptionAvailable = isCurrentUserOwnsEvent
            ),
            EventScreenItem.FormsHeader(
                itemId = EVENT_SCREEN_ITEM_ID.DATES_HEADER,
                title = "Время проведения",
                isExpanded = setOfExpandedItems.contains(EVENT_SCREEN_ITEM_ID.DATES_HEADER),
                items = TreeSet(), // TODO: отрефакторить: как избежать создание этого пустого mutableList? Он нужен просто как загулшка
                editOptionIcon = if (isCurrentUserOwnsEvent) R.drawable.ic_add else null,
                onEditOptionClick = {
                    openDatePicker(null)
                }
            ).apply {
                if (!eventBeforeEdit?.dates.isNullOrEmpty()) {
                    eventBeforeEdit!!.dates.forEach { timeInterval ->
                        items.add(EventScreenItem.EventDateItem(timeInterval, this))
                    }
                } else {
                    items.add(
                        NoDatesPlaceholderFactory.create(header = this)
                    )
                }
            },
            EventScreenItem.FormsHeader(
                itemId = EVENT_SCREEN_ITEM_ID.DESCRIPTION_HEADER,
                title = "Описание",
                isExpanded = setOfExpandedItems.contains(EVENT_SCREEN_ITEM_ID.DESCRIPTION_HEADER),
                items = TreeSet(),
                editOptionIcon = if (isCurrentUserOwnsEvent) R.drawable.ic_edit_blue else null,
                onEditOptionClick = { }
            ).apply {
                items.add(
                    if (eventBeforeEdit != null && !eventBeforeEdit!!.description.isNullOrBlank()) {
                        EventScreenItem.TextBox(
                            value = eventBeforeEdit!!.description!!,
                            header = this,
                            isEditLocked = true,
                            hasFocusIntent = false
                        )
                    } else {
                        NoDescriptionPlaceholderFactory.create(header = this)
                    }
                )

                onEditOptionClick = { headerPosition ->
                    editOptionIcon = null
                    enableDescriptionEdit(headerPosition)
                    eventScreenListPresenter.proeventFormsHeaderItemPresenter.absoluteFormsHeaderPresenter.updateAbsoluteFormsHeader()
                    viewState.showEditOptions()
                }
            },
            EventScreenItem.FormsHeader(
                itemId = EVENT_SCREEN_ITEM_ID.MAPS_HEADER,
                title = "Карты меропрития",
                isExpanded = setOfExpandedItems.contains(EVENT_SCREEN_ITEM_ID.MAPS_HEADER),
                items = TreeSet(),
                editOptionIcon = if (isCurrentUserOwnsEvent) R.drawable.ic_add else null,
                onEditOptionClick = {
                    viewState.showMessage("Данный функционал пока не реализован, так как для него нет законченного дизайна")
                }
            ).apply {
                items.add(NoItemsPlaceholderFactory.create(header = this))
            },
            EventScreenItem.FormsHeader(
                itemId = EVENT_SCREEN_ITEM_ID.POINTS_HEADER,
                title = "Точки",
                isExpanded = setOfExpandedItems.contains(EVENT_SCREEN_ITEM_ID.POINTS_HEADER),
                items = TreeSet(),
                editOptionIcon = if (isCurrentUserOwnsEvent) R.drawable.ic_add else null,
                onEditOptionClick = {
                    viewState.showMessage("Данный функционал пока не реализован, так как для него нет законченного дизайна")
                }
            ).apply {
                items.add(NoItemsPlaceholderFactory.create(header = this))
            },
            EventScreenItem.FormsHeader(
                itemId = EVENT_SCREEN_ITEM_ID.PARTICIPANTS_HEADER,
                title = "Участники",
                isExpanded = setOfExpandedItems.contains(EVENT_SCREEN_ITEM_ID.PARTICIPANTS_HEADER),
                items = TreeSet(),
                editOptionIcon = if (isCurrentUserOwnsEvent) R.drawable.ic_add else null,
                onEditOptionClick = {
                    localRouter.navigateTo(
                        screens.participantPickerTypeSelection(
                            pickedParticipantsIds
                        )
                    )
                }
            ).apply {
                val participantItems =
                    eventBeforeEdit?.participantsUserIds?.toParticipantItems(this)

                if (!participantItems.isNullOrEmpty()) {
                    participantItems.forEach {
                        items.add(it)
                    }
                } else {
                    items.add(
                        NoItemsPlaceholderFactory.create(header = this)
                    )
                }
            }
        )
    }

    inner class EventScreenListPresenter(
        val eventScreenItems: MutableList<EventScreenItem> = mutableListOf(),
        private var proeventTextFormItemClickListener: (
            (
            IProeventTextFormItemView,
            EventScreenItem
        ) -> Unit)? = null,
        private var proeventFormsHeaderItemClickListener: ((IProeventFormsHeaderItemView, EventScreenItem) -> Unit)? =
            null,
        override val proeventProfilePictureFormItemPresenter: IItemPresenter<IProeventProfilePictureItemView> =
            object :
                IItemPresenter<IProeventProfilePictureItemView> {
                override fun onItemClick(view: IProeventProfilePictureItemView) {
                    // TODO: stuff
                }

                override fun bindView(view: IProeventProfilePictureItemView) {
                    val pos = view.pos
                    with(view) {
                        with(eventScreenItems[pos] as EventScreenItem.ProfileImageForm) {
                            setText(text)
                        }
                    }
                }
            },
        override val proeventTextFormItemPresenter: IItemPresenter<IProeventTextFormItemView> = object :
            IItemPresenter<IProeventTextFormItemView> {
            override fun onItemClick(view: IProeventTextFormItemView) {
                proeventTextFormItemClickListener?.invoke(view, eventScreenItems[view.pos])
            }

            override fun bindView(view: IProeventTextFormItemView) {
                val pos = view.pos
                with(view) {
                    with(eventScreenItems[pos] as EventScreenItem.TextForm) {
                        setTitle(title)
                        setHint(hint)
                        setValue(value)
                        Log.d(
                            "[MYLOG]",
                            "pos: $pos; setValue: $value; eventScreenItems[pos].itemId: ${eventScreenItems[pos].itemId}"
                        )
                        setEditOption(isEditOptionAvailable)
                        setEditLock(isEditLocked)
                        setOnEditUnlockListener {
                            isEditLocked = false
                            viewState.showEditOptions()
                        }
                        setOnEditOptionHideListener {
                            isEditOptionAvailable = false
                        }
                        setOnValueChangedListener { newValue ->
                            Log.d(
                                "[MYLOG]",
                                "eventScreenItems[pos].itemId: ${eventScreenItems[pos].itemId} setOnValueChangedListener newValue: $newValue"
                            )
                            value = newValue
                        }
                    }
                }
            }
        },
        override val proeventFormsHeaderItemPresenter: IFormsHeaderItemPresenter =
            object :
                IFormsHeaderItemPresenter {

                private fun onExpandEvent(headerPosition: Int) {
                    viewState.hideKeyboard()
                    with(eventScreenItems[headerPosition] as EventScreenItem.FormsHeader<EventScreenItem.ListItem>) {
                        if (!isExpanded) {
                            addHeaderItemsToScreenItems(headerPosition, items)
                            setOfExpandedItems.add(eventScreenItems[headerPosition].itemId)
                            //viewState.eventScreenListNotifyItemRangeInserted(headerPosition + 1, items.size)
                        } else {
                            removeHeaderItemsFromScreenItems(headerPosition)
                            setOfExpandedItems.remove(eventScreenItems[headerPosition].itemId)
                            //viewState.eventScreenListNotifyItemRangeRemoved(headerPosition + 1, items.size)
                        }
                        this.isExpanded = !this.isExpanded
                    }
                    viewState.updateEventScreenList()
                }

                override val absoluteFormsHeaderPresenter =
                    object : IFormsHeaderItemPresenter.AbsoluteFormsHeaderPresenter() {
                        override fun showAbsoluteFormsHeader(currAbsoluteFormsHeaderId: EVENT_SCREEN_ITEM_ID) {
                            super.showAbsoluteFormsHeader(currAbsoluteFormsHeaderId)
                            val header =
                                eventScreenItems.find { item -> item.itemId == currAbsoluteFormsHeaderId } as EventScreenItem.FormsHeader<EventScreenItem.ListItem>
                            viewState.showAbsoluteFormsHeader(title = header.title,
                                editIcon = header.editOptionIcon,
                                editIconTint = null,
                                onCollapse = {
                                    onExpandEvent(headerPosition = eventScreenListPresenter.eventScreenItems.indexOfFirst { it == header })
                                    eventScreenListPresenter.proeventFormsHeaderItemPresenter.absoluteFormsHeaderPresenter.hideAbsoluteFormsHeader()
                                },
                                onCollapseScrollToPosition = eventScreenItems.indexOfFirst { it == header },
                                // TODO: отрефакторить: избавиться от использования headerPosition и передавать вместо этого прямую сслку на header
                                onEdit = { header.onEditOptionClick(eventScreenItems.indexOfFirst { item -> item.itemId == header.itemId }) })
                        }

                        override fun hideAbsoluteFormsHeader() {
                            super.hideAbsoluteFormsHeader()
                            viewState.hideAbsoluteBar()
                        }
                    }

                override fun onFirstVisibleItemPositionChangeListener(position: Int) {
                    with(eventScreenItems[position]) {
                        if (this is EventScreenItem.FormsHeader<*> && this.isExpanded) {
                            absoluteFormsHeaderPresenter.showAbsoluteFormsHeader(this.itemId)
                        } else if (this is EventScreenItem.ListItem && eventScreenItems[position + 1] is EventScreenItem.ListItem) {
                            absoluteFormsHeaderPresenter.showAbsoluteFormsHeader(this.header.itemId)
                        } else {
                            absoluteFormsHeaderPresenter.hideAbsoluteFormsHeader()
                        }
                    }
                }

                override fun onEditClick(view: IProeventFormsHeaderItemView) {
                    (eventScreenItems[view.pos] as EventScreenItem.FormsHeader<*>).onEditOptionClick(
                        view.pos
                    )
                }

                override fun onItemClick(view: IProeventFormsHeaderItemView) =
                    onExpandEvent(headerPosition = eventScreenListPresenter.eventScreenItems.indexOfFirst { item ->
                        if (item is EventScreenItem.FormsHeader<*>) {
                            item.itemId == view.itemId
                        } else {
                            false
                        }
                    })

                override fun bindView(view: IProeventFormsHeaderItemView) {
                    val pos = view.pos
                    with(eventScreenItems[pos] as EventScreenItem.FormsHeader<*>) {
                        view.itemId = itemId
                        view.setTitle(title)
                        view.setExpandState(isExpanded)
                        view.setEditOptionIcon(editOptionIcon)
                    }
                }
            },
        override val participantItemPresenter: IItemPresenter<IParticipantItemView> = object :
            IItemPresenter<IParticipantItemView> {
            override fun onItemClick(view: IParticipantItemView) {
                val profile =
                    participantProfiles[(eventScreenItems[view.pos] as EventScreenItem.ParticipantItem).participantId]!!
                localRouter.navigateTo(screens.eventParticipant(profile))
            }

            override fun bindView(view: IParticipantItemView) {
                val pos = view.pos
                with(view) {
                    with(eventScreenItems[pos] as EventScreenItem.ParticipantItem) {
                        participantProfiles[participantId]?.let {
                            setName(if (!it.fullName.isNullOrBlank()) it.fullName!! else if (!it.nickName.isNullOrBlank()) it.nickName!! else if (!it.email.isNullOrBlank()) it.email!! else "#$participantId")
                            setStatus(if (!it.description.isNullOrBlank()) it.description!! else if (!it.nickName.isNullOrBlank()) it.nickName!! else if (!it.email.isNullOrBlank()) it.email!! else "id пользователя: $participantId")
                        } ?: run {
                            setName("[LOADING]")
                            setStatus("[LOADING]")
                        }
                    }
                }
            }
        },
        override val eventDateItemPresenter: IEventDateItemPresenter =
            object : IEventDateItemPresenter {
                private fun isDateExpired(timeInterval: TimeInterval) =
                    (timeInterval.end < System.currentTimeMillis()) // TODO: отрефакторить: получать время от сущности получаемой через dagger

                override fun onItemClick(view: IEventDateItemView) {
                    if (!isCurrentUserOwnsEvent || isDateExpired((eventScreenItems[view.pos] as EventScreenItem.EventDateItem).timeInterval)) {
                        return
                    }
                    onEditClick(view)
                }

                override fun onEditClick(view: IEventDateItemView) {
                    if (!isCurrentUserOwnsEvent || isDateExpired((eventScreenItems[view.pos] as EventScreenItem.EventDateItem).timeInterval)) {
                        return
                    }
                    openDatePicker((eventScreenItems[view.pos] as EventScreenItem.EventDateItem).timeInterval)
                }

                override fun onRemoveClick(view: IEventDateItemView) {
                    val date =
                        (eventScreenItems[view.pos] as EventScreenItem.EventDateItem).timeInterval
                    if (!isCurrentUserOwnsEvent || isDateExpired(date)) {
                        return
                    }
                    removeDate(date)
                }

                override fun bindView(view: IEventDateItemView) = with(view) {
                    with((eventScreenItems[view.pos] as EventScreenItem.EventDateItem).timeInterval) {
                        setStartDate(start)
                        setEndDate(end)
                        val isDateExpired = isDateExpired(this)
                        setAsExpired(isDateExpired)
                        setEditOption(isCurrentUserOwnsEvent && !isDateExpired)
                    }
                }
            },
        override val noItemsPlaceholderItemPresenter: IItemPresenter<INoItemsPlaceholderItemView> = object :
            IItemPresenter<INoItemsPlaceholderItemView> {
            override fun onItemClick(view: INoItemsPlaceholderItemView) {
                // TODO("Not yet implemented")
            }

            override fun bindView(view: INoItemsPlaceholderItemView) =
                with(eventScreenItems[view.pos] as EventScreenItem.NoItemsPlaceholder) {
                    view.setDescription(description, spanImageRes, spanImagePos)
                }

        },

        override val textBoxPresenter: IItemPresenter<ITextBoxItemView> = object :
            IItemPresenter<ITextBoxItemView> {
            override fun onItemClick(view: ITextBoxItemView) {
                // TODO("Not yet implemented")
            }

            override fun bindView(view: ITextBoxItemView) = with(view) {
                with(eventScreenItems[view.pos] as EventScreenItem.TextBox) {
                    setValue(value)
                    setOnValueChangedListener { newValue -> value = newValue }
                    setEditLock(isEditLocked)
                    if (hasFocusIntent) {
                        requestFocus()
                        hasFocusIntent = false
                    }
                }
            }
        }
    ) : IEventScreenListPresenter {
        override fun getCount() = eventScreenItems.size

        override fun getType(position: Int) = eventScreenItems[position].type.ordinal

        fun setScreenItems(items: List<EventScreenItem>) {
            eventScreenItems.clear()
            eventScreenItems.addAll(items)
            viewState.updateEventScreenList()
        }
    }

    val eventScreenListPresenter =
        EventScreenListPresenter()

    private var isSaveAvailable = true

    private fun saveCallback(successEvent: Event?) {
        isSaveAvailable = true
        viewState.enableSaveOptions()
        if (successEvent == null) {
            return
        }
        eventBeforeEdit = successEvent
        viewState.hideEditOptions()
        cancelEdit()
    }

    fun saveEvent() {
        // TODO: вынести в кастомную вьюшку
        if (!isSaveAvailable) {
            return
        }
        isSaveAvailable = false
        viewState.disableSaveOptions()

        val description =
            with(eventScreenListPresenter.eventScreenItems.find { item -> item.itemId == EVENT_SCREEN_ITEM_ID.DESCRIPTION_HEADER } as EventScreenItem.FormsHeader<EventScreenItem.ListItem>) {
                if (items.first() is EventScreenItem.TextBox) {
                    (items.first() as EventScreenItem.TextBox).value
                } else {
                    ""
                }
            }

        // TODO: отрефакторить: вынести в extension функцию
        val dates =
            with(eventScreenListPresenter.eventScreenItems.find { item -> item.itemId == EVENT_SCREEN_ITEM_ID.DATES_HEADER } as EventScreenItem.FormsHeader<EventScreenItem.ListItem>) {
                if (items.first() is EventScreenItem.NoItemsPlaceholder) {
                    TreeSet()
                } else {
                    TreeSet<TimeInterval>().apply {
                        items.forEach {
                            add((it as EventScreenItem.EventDateItem).timeInterval)
                        }
                    }
                }
            }

        if (eventBeforeEdit == null) {
            addEvent(
                name = (eventScreenListPresenter.eventScreenItems[1] as EventScreenItem.TextForm).value,
                dates = dates,
                address = Address(
                    0.0,
                    0.0,
                    (eventScreenListPresenter.eventScreenItems[2] as EventScreenItem.TextForm).value
                ),
                description = description,
                uuid = null,
                callback = ::saveCallback
            )
        } else {
            val participantsItems =
                (eventScreenListPresenter.eventScreenItems.find { item -> item.itemId == EVENT_SCREEN_ITEM_ID.PARTICIPANTS_HEADER } as EventScreenItem.FormsHeader<EventScreenItem.ListItem>).items
            val participantsItemsIterator = participantsItems.iterator()
            val participantsUserIds =
                if (participantsItems.first() is EventScreenItem.NoItemsPlaceholder) {
                    null
                } else {
                    LongArray(
                        participantsItems.size
                    ) { return@LongArray (participantsItemsIterator.next() as EventScreenItem.ParticipantItem).participantId }
                }

            val eventAfterEdit = Event(
                id = eventBeforeEdit!!.id,
                name = (eventScreenListPresenter.eventScreenItems[1] as EventScreenItem.TextForm).value,
                ownerUserId = loginRepository.getLocalId()!!,
                status = Event.Status.ACTUAL,
                description = description,
                dates = dates,
                participantsUserIds = participantsUserIds,
                city = "PLACEHHOLDER",
                address = Address(
                    0.0,
                    0.0,
                    (eventScreenListPresenter.eventScreenItems[2] as EventScreenItem.TextForm).value
                ),
                mapsFileIds = null,
                pointsPointIds = null,
                imageFile = null
            )
            editEvent(
                eventAfterEdit,
                callback = ::saveCallback
            )
        }
    }

    fun cancelEdit() {
        eventBeforeEdit?.let {
            eventScreenListPresenter.setScreenItems(
                getEventScreenItemsBeforeEdit().apply {
                    setOfExpandedItems.forEach {
                        val headerPosition = indexOfFirst { item -> item.itemId == it }
                        addAll(
                            headerPosition + 1,
                            (get(headerPosition) as EventScreenItem.FormsHeader<EventScreenItem.ListItem>).items
                        )
                    }
                })
            // Log.d("[MYLOG]", "eventScreenListPresenter.eventScreenItems: ${eventScreenListPresenter.eventScreenItems}") // TODO: эта строчка ломает kotlin?
            eventScreenListPresenter.proeventFormsHeaderItemPresenter.absoluteFormsHeaderPresenter.updateAbsoluteFormsHeader()
            viewState.updateEventScreenList()
            viewState.hideEditOptions()
        } ?: run {
            localRouter.exit()
        }
    }

    private val pickedParticipantsIds: List<Long>
        get() {
            val participantsListItems =
                (eventScreenListPresenter.eventScreenItems.find { item -> item.itemId == EVENT_SCREEN_ITEM_ID.PARTICIPANTS_HEADER } as EventScreenItem.FormsHeader<EventScreenItem.ListItem>).items
            return if (participantsListItems.first() is EventScreenItem.NoItemsPlaceholder) {
                listOf()
            } else {
                with(participantsListItems.iterator()) {
                    List(participantsListItems.size) {
                        (next() as EventScreenItem.ParticipantItem).participantId
                    }
                }
            }
        }
    private var isParticipantsProfilesInitialized = false

    val pickedDates = mutableListOf<TimeInterval>()
    private var isDatesInitialized = false

    fun addEvent(
        name: String,
        dates: TreeSet<TimeInterval>,
        address: Address?,
        description: String,
        uuid: String?,
        callback: ((Event?) -> Unit)? = null
    ) {
        eventsRepository
            .saveEvent(
                Event(
                    id = null,
                    name = name,
                    ownerUserId = loginRepository.getLocalId()!!,
                    status = Event.Status.ACTUAL,
                    dates = dates,
                    description = description,
                    participantsUserIds = pickedParticipantsIds.toLongArray(),
                    city = null,
                    address = address,
                    mapsFileIds = null,
                    pointsPointIds = null,
                    imageFile = uuid
                )
            )
            .observeOn(uiScheduler)
            .subscribe({
                callback?.invoke(it)
                viewState.showMessage(getString(R.string.event_created))
                viewState.hideEditOptions()
                viewState.enableActionOptions()
            }, {
                callback?.invoke(null)
                viewState.showMessage(getString(R.string.error_occurred, it.message))
            }).disposeOnDestroy()
    }

    fun editEvent(event: Event, callback: ((Event?) -> Unit)? = null) {
        event.participantsUserIds = pickedParticipantsIds.toLongArray()
        eventsRepository
            .editEvent(event)
            .observeOn(uiScheduler)
            .subscribe({
                callback?.invoke(event)
                viewState.showMessage(getString(R.string.changes_saved))
            }, {
                callback?.invoke(null)
                viewState.showMessage(getString(R.string.error_occurred, it.message))
            }).disposeOnDestroy()
    }

    fun saveImage(file: File, callback: ((String?) -> Unit)? = null) {
        imagesRepository
            .saveImage(file)
            .observeOn(uiScheduler)
            .subscribe({
                callback?.invoke(it.uuid)
            }, {
                callback?.invoke(null)
            }).disposeOnDestroy()
    }

    fun deleteImage(uuid: String) {
        imagesRepository.deleteImage(uuid).subscribe().disposeOnDestroy()
    }

    private fun finishEvent() =
        localRouter.navigateTo(
            screens.eventActionConfirmation(
                eventBeforeEdit!!,
                Event.Status.COMPLETED
            )
        )

    fun cancelEvent(event: Event) =
        localRouter.navigateTo(screens.eventActionConfirmation(event, Event.Status.CANCELLED))

    private fun deleteEvent() =
        localRouter.navigateTo(screens.eventActionConfirmation(eventBeforeEdit!!, null))

    private fun copyEvent() {
        // TODO: вывести предпреждения что не сохранённые данные не будут перенесены в копию? Уточнить у дизайнера
        val event = eventBeforeEdit!!
        event.ownerUserId = loginRepository.getLocalId()!!
        event.status = Event.Status.ACTUAL
        eventsRepository
            .saveEvent(event)
            .observeOn(uiScheduler)
            .subscribe({
                localRouter.navigateTo(screens.event(it))
            }, {
                viewState.showMessage(getString(R.string.error_occurred, it.message))
            }).disposeOnDestroy()
    }

    fun pickParticipants() {
        localRouter.navigateTo(screens.participantPickerTypeSelection(pickedParticipantsIds))
    }

    fun pickDates() {
        Toast.makeText(ProEventApp.instance, "EventPresenter::pickDates call", Toast.LENGTH_LONG)
            .show()
    }

    fun openDateEditOptions(timeInterval: TimeInterval) {
        viewState.showDateEditOptions(pickedDates.indexOf(timeInterval))
    }

    // TODO: отрефакторить: копирует addParticipantsProfiles
    private fun addDateItemView(timeInterval: TimeInterval) {
        // TODO: отрефакторить: перенести это в eventScreenListPresenter
        val headerPosition =
            eventScreenListPresenter.eventScreenItems.indexOfFirst { item -> item.itemId == EVENT_SCREEN_ITEM_ID.DATES_HEADER }
        with(eventScreenListPresenter.eventScreenItems[headerPosition] as EventScreenItem.FormsHeader<EventScreenItem.ListItem>) {
            if (items.first() is EventScreenItem.NoItemsPlaceholder) {
                items.clear()
                if (isExpanded) {
                    eventScreenListPresenter.eventScreenItems.subList(
                        headerPosition + 1,
                        headerPosition + 2
                    ).clear()
                }
            }
            val listItemPosition =
                with(items.indexOfFirst { item -> (item as EventScreenItem.EventDateItem).timeInterval.start > timeInterval.start }) {
                    if (this == -1) {
                        items.size
                    } else {
                        this
                    }
                }
            Log.d("[MYLOG]", "listItemPosition($listItemPosition)")
            val screenItemPosition = headerPosition + 1 + listItemPosition
            items.add(
                EventScreenItem.EventDateItem(
                    timeInterval = timeInterval,
                    header = this
                )
            )
            if (isExpanded) {
                eventScreenListPresenter.eventScreenItems.add(
                    screenItemPosition,
                    EventScreenItem.EventDateItem(
                        timeInterval = timeInterval,
                        header = this
                    )
                )
            }
            if (!isExpanded) {
                isExpanded = true
                setOfExpandedItems.add(EVENT_SCREEN_ITEM_ID.DATES_HEADER)
                addHeaderItemsToScreenItems(headerPosition, items)
            }
        }
        viewState.updateEventScreenList()
        viewState.showEditOptions()
    }

    fun initParticipantsProfiles(participantsIds: LongArray) {
//        if (isParticipantsProfilesInitialized) {
//            return
//        }
//        isParticipantsProfilesInitialized = true
//        for (id in participantsIds) {
//            profilesRepository.getProfile(id)
//                .observeOn(uiScheduler)
//                .subscribe({ profileDto ->
//                    addParticipantItemView(profileDto!!)
//                }, {
//                    val profile = Profile(
//                        id = id,
//                        fullName = "Заглушка",
//                        description = "Профиля нет, или не загрузился",
//                    )
//                    addParticipantItemView(profile)
//                }).disposeOnDestroy()
//        }
    }

    fun initDates(dates: TreeSet<TimeInterval?>) {
        if (isDatesInitialized) {
            return
        }
        isDatesInitialized = true
        for (date in dates) {
            date?.let { addDateItemView(it) }
        }
    }

    // TODO: отрефакторить: передавать только id
    fun addParticipantsProfiles(participants: Array<Profile>) {
//        for (participant in participants) {
//            addParticipantItemView(participant)
//        }

        // TODO: отрефакторить: перенести это в eventScreenListPresenter
        val headerPosition =
            eventScreenListPresenter.eventScreenItems.indexOfFirst { item -> item.itemId == EVENT_SCREEN_ITEM_ID.PARTICIPANTS_HEADER }
        with(eventScreenListPresenter.eventScreenItems[headerPosition] as EventScreenItem.FormsHeader<EventScreenItem.ListItem>) {
            if (items.first() is EventScreenItem.NoItemsPlaceholder) {
                items.clear()
                if (isExpanded) {
                    eventScreenListPresenter.eventScreenItems.subList(
                        headerPosition + 1,
                        headerPosition + 2
                    ).clear()
                }
            }
            val prevLastItemPosition = headerPosition + 1 + items.size
            for (participantProfile in participants) {
                participantProfiles[participantProfile.id] = participantProfile
                items.add(
                    EventScreenItem.ParticipantItem(
                        participantId = participantProfile.id,
                        header = this
                    )
                )
                if (isExpanded) {
                    eventScreenListPresenter.eventScreenItems.add(
                        prevLastItemPosition,
                        EventScreenItem.ParticipantItem(
                            participantId = participantProfile.id,
                            header = this
                        )
                    )
                }
            }
            if (!isExpanded) {
                isExpanded = true
                setOfExpandedItems.add(EVENT_SCREEN_ITEM_ID.PARTICIPANTS_HEADER)
                addHeaderItemsToScreenItems(headerPosition, items)
            }
        }
        viewState.updateEventScreenList()
        viewState.showEditOptions()
    }

    fun openDatePicker(timeInterval: TimeInterval?) {
        localRouter.navigateTo(screens.eventDatesPicker(timeInterval))
    }

    fun addEventDate(timeInterval: TimeInterval) {
        addDateItemView(timeInterval)
    }

    fun clearDates() {
        viewState.clearDates()
        pickedDates.clear()
        isDatesInitialized = false
    }

    fun clearParticipants() {
        viewState.clearParticipants()
        //pickedParticipantsIds.clear()
        isParticipantsProfilesInitialized = false
    }

    fun addEventPlace(address: Address?) {
        localRouter.navigateTo(screens.addEventPlace(address))
    }

    fun enableDescriptionEdit() {
        viewState.enableDescriptionEdit()
    }

    fun expandDescription() {
        viewState.expandDescription()
    }

    fun expandMaps() {
        viewState.expandMaps()
    }

    fun expandPoints() {
        viewState.expandPoints()
    }

    fun expandParticipants() {
        viewState.expandParticipants()
    }

    fun expandDates() {
        viewState.expandDates()
    }

    fun hideEditOptions() {
        viewState.hideEditOptions()
    }

    fun lockEdit() {
        viewState.lockEdit()
    }

    fun showMessage(message: String) {
        viewState.showMessage(message)
    }

//    fun showAbsoluteBar(
//        title: String,
//        iconResource: Int?,
//        iconTintResource: Int?,
//        onCollapseScroll: Int,
//        onCollapse: () -> Unit,
//        onEdit: () -> Unit
//    ) {
//        viewState.showAbsoluteBar(
//            title,
//            iconResource,
//            iconTintResource,
//            onCollapseScroll,
//            onCollapse,
//            onEdit
//        )
//    }

    fun unlockNameEdit() {
        viewState.unlockNameEdit()
    }

    fun unlockLocationEdit() {
        viewState.unlockLocationEdit()
    }

    // TODO: отрефакторить: копирует removeDateItem()
    private fun removeParticipantItem(id: Long) {
        val headerPosition =
            eventScreenListPresenter.eventScreenItems.indexOfFirst { item -> item.itemId == EVENT_SCREEN_ITEM_ID.PARTICIPANTS_HEADER }
        with(eventScreenListPresenter.eventScreenItems[headerPosition] as EventScreenItem.FormsHeader<EventScreenItem.ListItem>) {
            val indexOfItemToRemove =
                items.indexOfFirst { item -> (item as EventScreenItem.ParticipantItem).participantId == id }
            if (indexOfItemToRemove == -1) {
                throw RuntimeException("Попытка удалить участника, который не явялется участником редактируемого мероприятия.")
            }
            items.removeIf { item -> (item as EventScreenItem.ParticipantItem).participantId == id }
            if (!isExpanded) {
                isExpanded = true
                setOfExpandedItems.add(EVENT_SCREEN_ITEM_ID.DATES_HEADER)
            } else {
                eventScreenListPresenter.eventScreenItems.removeAt(headerPosition + 1 + indexOfItemToRemove)
            }
            if (items.isEmpty()) {
                items.add(NoItemsPlaceholderFactory.create(header = this))
                eventScreenListPresenter.eventScreenItems.add(
                    headerPosition + 1,
                    items.first()
                )
            }
        }
        viewState.updateEventScreenList()
        viewState.showEditOptions()
    }

    fun removeParticipant(id: Long) {
        removeParticipantItem(id)
        // .toList() используется чтобы передать именно копию pickedParticipantsIds, а не ссылку
//        viewState.removeParticipant(id, pickedParticipantsIds.toList())
//        viewState.showEditOptions()
        // pickedParticipantsIds.remove(id)
    }

    private fun removeDateItem(timeInterval: TimeInterval) {
        val headerPosition =
            eventScreenListPresenter.eventScreenItems.indexOfFirst { item -> item.itemId == EVENT_SCREEN_ITEM_ID.DATES_HEADER }
        with(eventScreenListPresenter.eventScreenItems[headerPosition] as EventScreenItem.FormsHeader<EventScreenItem.ListItem>) {
            val indexOfItemToRemove =
                items.indexOfFirst { item -> (item as EventScreenItem.EventDateItem).timeInterval == timeInterval }
            if (indexOfItemToRemove == -1) {
                throw RuntimeException("Попытка удалить дату(временной интервал), которая отсутствует в датах редактируемого мероприятия.")
            }
            items.removeIf { item -> (item as EventScreenItem.EventDateItem).timeInterval == timeInterval }
            if (!isExpanded) {
                isExpanded = true
                setOfExpandedItems.add(EVENT_SCREEN_ITEM_ID.DATES_HEADER)
            } else {
                eventScreenListPresenter.eventScreenItems.removeAt(headerPosition + 1 + indexOfItemToRemove)
            }
            if (items.isEmpty()) {
                items.add(NoDatesPlaceholderFactory.create(header = this))
                eventScreenListPresenter.eventScreenItems.add(
                    headerPosition + 1,
                    items.first()
                )
            }
        }
        viewState.updateEventScreenList()
        viewState.showEditOptions()
    }

    fun removeDate(timeInterval: TimeInterval) {
        removeDateItem(timeInterval)
//        viewState.removeDate(timeInterval, pickedDates.toList())
//        pickedDates.remove(timeInterval)
    }

    fun removeDate(position: Int) {
        viewState.removeDate(pickedDates[position], pickedDates.toList())
        pickedDates.removeAt(position)
    }

    fun editDate(position: Int) {
        Toast.makeText(
            ProEventApp.instance,
            "EventPresenter::editDate call;\npickedDates[position]: ${pickedDates[position]};",
            Toast.LENGTH_LONG
        )
            .show()
    }

    fun hideDateEditOptions() {
        viewState.hideDateEditOptions()
    }

    private fun loadProfiles() {
        for (participantItem in (eventScreenListPresenter.eventScreenItems[7] as EventScreenItem.FormsHeader<*>).items) {
            if (participantItem is EventScreenItem.NoItemsPlaceholder) {
                return
            }
            profilesRepository.getProfile((participantItem as EventScreenItem.ParticipantItem).participantId)
                .observeOn(uiScheduler)
                .subscribe({ profileDto ->
                    participantProfiles[participantItem.participantId] = profileDto!!
                    viewState.updateEventScreenList()
                }, {
                    val profile = Profile(
                        id = participantItem.participantId,
                        fullName = "[ОШИБКА]",
                        description = "Профиля нет, или не загрузился",
                    )
                    participantProfiles[participantItem.participantId] = profile
                    viewState.updateEventScreenList()
                }).disposeOnDestroy()
        }
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        eventScreenListPresenter.eventScreenItems.addAll(getEventScreenItemsBeforeEdit())
        viewState.init()
        if (eventBeforeEdit != null) {
            viewState.enableActionOptions()
        } else {
            viewState.showEditOptions()
        }
        loadProfiles()
    }


    fun getEventActionOptions(): List<Pair<String, () -> Unit>> {
        Log.d("[MYLOG]", "getEventActionOptions")
        return if (eventBeforeEdit!!.ownerUserId == loginRepository.getLocalId()!!) {
            if (eventBeforeEdit!!.status == Event.Status.ACTUAL) {
                listOf(
                    Pair("Заваершить мероприятие") { finishEvent() },
                    Pair("Скопировать мероприятие") { copyEvent() },
                    Pair("Удалить мероприятие") { deleteEvent() }
                )
            } else {
                listOf(
                    Pair("Скопировать мероприятие") { copyEvent() },
                    Pair("Удалить мероприятие") { deleteEvent() }
                )
            }
        } else {
            listOf(
                Pair("Скопировать мероприятие") { copyEvent() },
            )
        }
    }
}