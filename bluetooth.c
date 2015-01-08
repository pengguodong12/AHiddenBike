//P2.0--S1，P2.4--DS，P2.5--SHCP，P2.6--STCP，控制最右边的数码管显示接收到的消息
//P1.1--RXD接蓝牙txd，P1.2--TXD 接蓝牙rxd
//P1.5--L6、P1.6--L7/Buzz
//采用TA0中断方式对LED亮的时间进行计时控制
//采用TA1中断方式对Buzz蜂鸣的时间进行计时控制
#include "io430.h"
#include "in430.h"

//蓝牙异步串口通信初始化
void USCIA0_init(void);

// 时钟源初始化
void CLK_init(void);

//TA0初始化，并开始计数
void init_TA0(void);

//TA1初始化，并开始计数
void init_TA1(void);

//TA0停止计数
void stop_TA0(void);

//TA1停止计数
void stop_TA1(void);

//灯、蜂鸣器初始化
void Buzz_init(void);

//数码管初始化
void Tube_init(void);

//数码管显示接收的蓝牙消息，num表示要显示的数字
void display(unsigned char num);

//接收到的单个字符
unsigned char buffer;

//数码管显示的数字编码
const char LEDtab[16]={0xC0, 0xF9, 0xA4, 0xB0, 0x99, 0x92,
0x82, 0xF8,0x80, 0x90, 0x88, 0x83, 0xC6, 0xA1, 0x86, 0x8E};

int main( void )
{
	WDTCTL = WDTPW + WDTHOLD;//关闭看门狗
	char temp = 0xff; 		//代表蓝牙传给单片机的字符

	__disable_interrupt();	//关总中断

	CLK_init();				//时钟源初始化
	Tube_init();			//数码管初始化
	Buzz_init();			//灯、蜂鸣器初始化
	USCIA0_init();    		//蓝牙异步串口通信初始化
	
	__enable_interrupt();	//开总中断
	LPM0;					//进入低功耗
	while(1)
	{
		temp = buffer;

		switch (temp) 		//根据接收的不同消息进行不同操作
		{
		case 1:          	//亮灯
			P1OUT &= ~BIT5;
			UCA0TXBUF = 1;	//回传成功亮灯信号
			init_TA0();		//开始计时10s，10s后自动灭灯
			break;
		case 2:          	//灭灯
			stop_TA0();		//停止计时
			P1OUT |= BIT5;
			UCA0TXBUF = 2;	//回传成功灭灯信号
			
			break;
		case 3:
			P1OUT &= ~BIT6;	//发声
			UCA0TXBUF = 3;	//回传成功发声信号
			init_TA1();		//开始计时10s，10s后自动关声
			break;
		case 4:
			stop_TA1();		//停止计时
			P1OUT |= BIT6;	//关声
			UCA0TXBUF = 4;	//回传成功关声信号
			
			break;
		default:
			break;
		}
		display(temp);		//数码管显示接收的蓝牙消息
		temp =	0xff;
		IE2 |= UCA0RXIE;	//打开接收中断
		LPM0;				//进入低功耗
	}

	return 0;
}

//蓝牙接收数据中断
#pragma vector=USCIAB0RX_VECTOR
__interrupt void UCA0RX_isr(void) {
	buffer = UCA0RXBUF;		//接收一个字符并保存
	IE2 &=~UCA0RXIE;		//关闭接收中断
	LPM0_EXIT;				//退出低功耗
}

//TimerA0 CCR0中断
#pragma vector=TIMER0_A0_VECTOR
__interrupt void timer0_A0_isr(void) {
	stop_TA0();				//停止TA0的计数
	P1OUT |= BIT5;			//灭灯
	UCA0TXBUF = 2;			//回传成功灭灯信号
	TA0CCTL0 &= ~CCIFG;		//中断标志清零
}

//TimerA1 CCR0中断
#pragma vector=TIMER1_A0_VECTOR
__interrupt void timer0_A1_isr(void) {
	stop_TA1();				//停止TA1的计数
	P1OUT |= BIT6;			//关声
	UCA0TXBUF = 4;			//回传成功关声信号
	TA1CCTL0 &= ~CCIFG;		//中断标志清零
}

// 时钟源初始化
void CLK_init(void) {
	//设置DCO的频率，SMCLK的时钟源默认为DCO
    BCSCTL1=CALBC1_12MHZ;
    DCOCTL=CALDCO_12MHZ; 
	
	BCSCTL3 |= LFXT1S_2;    //ACLK选择12kHZ的VLOCLK
	BCSCTL1 |= DIVA_0;		//ACLK 1分频	
}

//蓝牙异步串口初始化
void USCIA0_init(void){
	UCA0CTL1 |= UCSWRST;				//软件复位
	P1SEL |= BIT1+BIT2;					//P1.1--RXD接蓝牙txd   P1.2--TXD 接蓝牙rxd
	P1SEL2 |= BIT1+BIT2;
	//UCA0CTL0  无校验，停止位长度1位，发送顺序先低位，数据位8位
	UCA0CTL1 |= UCSSEL_2+UCRXEIE;		//时钟源SMCLK，
	UCA0BR1 = 0x04;						//波特率9600，频率12MHZ，波特率因子1250        
	UCA0BR0 = 0xE2;
	UCA0MCTL = 0;
	UCA0CTL1 &= ~UCSWRST;
	IE2 |= UCA0RXIE;					//接收中断允许
}

//TA0初始化，并开始计数
void init_TA0(void) {
	//清TA0R=0, ACLK为计数时钟，增计数方式，4分频，即频率为3000Hz
	TA0CTL |= TACLR;
    TA0CTL |= TASSEL_1 + MC_1 + ID_2;
	TA0CCR0=30000;				//置TA0CCR0值，10秒
	TA0CCTL0 |= CCIE;			//允许CCR0中断
}

//TA1初始化，并开始计数
void init_TA1(void) {
	//清TA1R=0, ACLK为计数时钟，增计数方式，4分频，即频率为3000Hz
	TA1CTL |= TACLR;
    TA1CTL |= TASSEL_1 + MC_1 + ID_2;
	TA1CCR0=30000;				//置TA1CCR0值，10秒
	TA1CCTL0 |= CCIE;			//允许CCR0中断
}

//停止TA0的计数
void stop_TA0(void){
	//清TA0R=0, 不计数，关闭CCR0中断
	TA0CCTL0 &= ~CCIE;
        TA0CTL &= ~(MC0+MC1);
    	TA0CTL |= TACLR;
}

//停止TA1的计数
void stop_TA1(void){
	//清TA1R=0, 不计数，关闭CCR0中断
	TA1CCTL0 &= ~CCIE;
    TA1CTL &= ~(MC0+MC1);
    	TA1CTL |= TACLR;
}

//LED、Buzz初始化
void Buzz_init(void) {
	P1SEL &= ~(BIT5+BIT6);
	P1SEL2 &= ~(BIT5+BIT6);
	P1OUT |= (BIT5+BIT6);
	P1DIR |= (BIT5+BIT6);
}

//数码管初始化
void Tube_init(void) {
	P2SEL &= ~(BIT0+BIT4+BIT5+BIT6);
	P2SEL2 &= ~(BIT0+BIT4+BIT5+BIT6);
	P2OUT &= ~(BIT0+BIT5+BIT6);
	P2OUT |= BIT4;
	P2DIR |= (BIT0+BIT4+BIT5+BIT6);
}

//最右边的数码管显示接收到的消息，num表示要显示的数字
void display(unsigned char num){
	unsigned char i, tmp;
	//传送数据到DS上
	P2OUT &= ~BIT0;
	for (i=0;i<8;i++){
		P2OUT |= BIT4;
		tmp = ((((LEDtab[num]) << i) >> 3) & BIT4);	//提取要输到DS上的数据
		P2OUT &= (~BIT4|tmp); 						//DS设置为要发送出去的数
		P2OUT |= BIT5;								//给SHCP一个脉冲
		P2OUT &= (~BIT5);
	}
	P2OUT |= BIT6;									//给STCP一个脉冲，使8位数据输出
	P2OUT &= (~BIT6);
	P2OUT |= BIT0;									//选择最右边的数码管
}
