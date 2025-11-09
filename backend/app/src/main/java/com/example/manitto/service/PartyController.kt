package com.example.manitto.service

import party.manitto.party.PartyRepository
import org.springframework.stereotype.Service
import party.manitto.party.Party

@Service
class PartyService(
    private val partyRepository: PartyRepository
) {

    fun createParty(name: String, password: String): Party {
        val newParty = Party(name = name, password = password)
        return partyRepository.save(newParty)
    }

    fun getAllParties(): List<Party> = partyRepository.findAll()
}