package com.neuralbit.letsnote.utilities

import com.neuralbit.letsnote.firebase.entities.NoteFire
import org.apache.commons.lang3.builder.CompareToBuilder

class NoteComparator : Comparator<NoteFire> {
    override fun compare(n1: NoteFire?, n2: NoteFire?): Int {
        return CompareToBuilder()
            .append(n2?.timeStamp, n1?.timeStamp)
            .toComparison()
    }
}