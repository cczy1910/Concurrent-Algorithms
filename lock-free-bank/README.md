# Lock-Free Bank
Потокобезопасная реализация гипотетического банка с использованием CAS-N.
В реализации используются основные идеи из работы 
"A Practical Multi-Word Compare-and-Swap Operation" by T. L. Harris,
c упрощенной (ускоренной) операцией DCSS за счет отсутвия проблемы ABA. 
