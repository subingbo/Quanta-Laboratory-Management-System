package com.ruoyi.qt.service.impl;

import java.util.Date;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.qt.domain.QtWorkstationReservation;
import com.ruoyi.qt.mapper.QtWorkstationReservationMapper;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QtWorkstationReservationServiceImplTest
{
    @Mock
    private QtWorkstationReservationMapper mapper;

    @InjectMocks
    private QtWorkstationReservationServiceImpl service;

    @Test
    void insertRejectsOverlap()
    {
        QtWorkstationReservation reservation = slot();
        when(mapper.countOverlapping(eq(2L), any(), any(), isNull())).thenReturn(1);
        assertThrows(ServiceException.class, () -> service.insertQtWorkstationReservation(reservation));
        verify(mapper, never()).insertQtWorkstationReservation(any());
    }

    @Test
    void insertAcceptsFreeSlot()
    {
        QtWorkstationReservation reservation = slot();
        when(mapper.countOverlapping(eq(2L), any(), any(), isNull())).thenReturn(0);
        when(mapper.insertQtWorkstationReservation(any())).thenReturn(1);
        service.insertQtWorkstationReservation(reservation);
        verify(mapper).insertQtWorkstationReservation(any());
    }

    private QtWorkstationReservation slot()
    {
        QtWorkstationReservation reservation = new QtWorkstationReservation();
        reservation.setWorkstationId(2L);
        reservation.setReserveStart(new Date(1_700_000_000_000L));
        reservation.setReserveEnd(new Date(1_700_003_600_000L));
        return reservation;
    }
}
