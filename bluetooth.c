//P2.0--S1��P2.4--DS��P2.5--SHCP��P2.6--STCP���������ұߵ��������ʾ���յ�����Ϣ
//P1.7--Buzz��P1.1--RXD������txd��P1.2--TXD ������rxd
//P1.5--L6��P1.6--L7
#include "io430.h"
#include "in430.h"

//�����첽����ͨ�ų�ʼ��
void USCIA0_init(void);

// ʱ��Դ��ʼ��
void DCO_init(void);

//�ơ���������ʼ��
void Buzz_init(void);

//����ܳ�ʼ��
void Tube_init(void);

//�������ʾ���յ�������Ϣ��num��ʾҪ��ʾ������
void display(unsigned char num);

//���յ��ĵ����ַ�
unsigned char buffer;

//�������ʾ�����ֱ���
const char LEDtab[16]={0xC0, 0xF9, 0xA4, 0xB0, 0x99, 0x92,
0x82, 0xF8,0x80, 0x90, 0x88, 0x83, 0xC6, 0xA1, 0x86, 0x8E};

int main( void )
{
	WDTCTL = WDTPW + WDTHOLD;//�رտ��Ź�
	char temp = 0xff; 		//��������������Ƭ�����ַ�

	__disable_interrupt();	//�����ж�

	DCO_init();				//ʱ��Դ��ʼ��
	Tube_init();			//����ܳ�ʼ��
	Buzz_init();			//�ơ���������ʼ��
	USCIA0_init();    		//�����첽����ͨ�ų�ʼ��
	
	__enable_interrupt();	//�����ж�
	LPM0;					//����͹���
	while(1)
	{
		temp = buffer;

		switch (temp) 		//���ݽ��յĲ�ͬ��Ϣ���в�ͬ����
		{
		case 1:          	//����
			P1OUT &= ~BIT5;
			UCA0TXBUF = 1;	//�ش��ɹ������ź�
			break;
		case 2:          	//���
			P1OUT |= BIT5;
			UCA0TXBUF = 2;	//�ش��ɹ�����ź�
			break;
		case 3:
			//P1OUT &= ~BIT7;	//����
			P1OUT &= ~BIT6;
			UCA0TXBUF = 3;	//�ش��ɹ������ź�
			break;
		case 4:
			//P1OUT |= BIT7; 	//����
			P1OUT |= BIT6;
			UCA0TXBUF = 4;	//�ش��ɹ������ź�
			break;
		default:
			break;
		}
		display(temp);		//�������ʾ���յ�������Ϣ
		temp =	0xff;
		IE2 |= UCA0RXIE;	//�򿪽����ж�
		LPM0;				//����͹���
	}

	return 0;
}

// ʱ��Դ��ʼ��
void DCO_init(void) {
	//����DCO��Ƶ�ʣ�SMCLK��ʱ��ԴĬ��ΪDCO
    BCSCTL1=CALBC1_12MHZ;
    DCOCTL=CALDCO_12MHZ; 
}

//�����첽���ڳ�ʼ��
void USCIA0_init(void){
	UCA0CTL1 |= UCSWRST;				//�����λ
	P1SEL |= BIT1+BIT2;					//P1.1--RXD������txd   P1.2--TXD ������rxd
	P1SEL2 |= BIT1+BIT2;
	//UCA0CTL0  ��У�飬ֹͣλ����1λ������˳���ȵ�λ������λ8λ
	UCA0CTL1 |= UCSSEL_2+UCRXEIE;		//ʱ��ԴSMCLK��
	UCA0BR1 = 0x04;						//������9600��Ƶ��12MHZ������������1250        
	UCA0BR0 = 0xE2;
	UCA0MCTL = 0;
	UCA0CTL1 &= ~UCSWRST;
	IE2 |= UCA0RXIE;					//�����ж�����
}

//LED��Buzz��ʼ��
void Buzz_init(void) {
	P1SEL &= ~(BIT5+BIT6+BIT7);
	P1SEL2 &= ~(BIT5+BIT6+BIT7);
	P1OUT |= (BIT5+BIT6+BIT7);
	P1DIR |= (BIT5+BIT6+BIT7);
}

//����ܳ�ʼ��
void Tube_init(void) {
	P2SEL &= ~(BIT0+BIT4+BIT5+BIT6);
	P2SEL2 &= ~(BIT0+BIT4+BIT5+BIT6);
	P2OUT &= ~(BIT0+BIT5+BIT6);
	P2OUT |= BIT4;
	P2DIR |= (BIT0+BIT4+BIT5+BIT6);
}

//�������������ж�
#pragma vector=USCIAB0RX_VECTOR
__interrupt void UCA0RX_isr(void) {
	buffer = UCA0RXBUF;		//����һ���ַ�������
	IE2 &=~UCA0RXIE;		//�رս����ж�
	LPM0_EXIT;				//�˳��͹���
}

//���ұߵ��������ʾ���յ�����Ϣ��num��ʾҪ��ʾ������
void display(unsigned char num){
	unsigned char i, tmp;
	//�������ݵ�DS��
	P2OUT &= ~BIT0;
	for (i=0;i<8;i++){
		P2OUT |= BIT4;
		tmp = ((((LEDtab[num]) << i) >> 3) & BIT4);	//��ȡҪ�䵽DS�ϵ�����
		P2OUT &= (~BIT4|tmp); 						//DS����ΪҪ���ͳ�ȥ����
		P2OUT |= BIT5;								//��SHCPһ������
		P2OUT &= (~BIT5);
	}
	P2OUT |= BIT6;									//��STCPһ�����壬ʹ8λ�������
	P2OUT &= (~BIT6);
	P2OUT |= BIT0;									//ѡ�����ұߵ������
}
