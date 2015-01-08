//P2.0--S1，P2.4--DS，P2.5--SHCP，P2.6--STCP，控制最右边的数码管显示接收到的消息
//P1.1--RXD接蓝牙txd，P1.2--TXD 接蓝牙rxd
//P1.5--L6、P1.6--L7、P2.1--Buzz,蜂鸣采用TA1输出的PWM来播放音乐
//采用TA0中断方式对LED亮的时间进行计时控制——10s
//采用WDT中断方式对Buzz蜂鸣的时间进行计时控制——20s
//采用TA1输出PWM从而输出音乐
#include "io430.h"
#include "in430.h"

const unsigned char music[2][34]={{6,3,2,3,2,2,3,3,1,2,3,6,1,6,1,6,6,6,1,1,6,1,2,2,4,4,4,1,1,2,3,3,3,3},
								  {3,2,3,1,1,7,5,5,6,0,1,2,1,3,0,3,3,3,3,3,3,4,3,2,1,0,5,6,4,3,1,3,2,2}};  //储存歌曲1、2音符唱名
						 
const unsigned char lvl[2][34]={{1,2,2,2,2,2,2,2,2,2,2,1,2,1,2,1,1,1,2,2,1,2,2,2,2,2,2,2,2,2,2,2,2,2},
								{4,4,4,4,4,2,2,2,2,1,4,4,4,4,1,4,4,4,4,4,4,4,4,4,4,1,2,2,4,4,4,4,4,4}};  //储存歌曲1、2音阶，低音1，中音2，高音3
						 
const unsigned char beat[2][34]= {{2,1,1,1,1,1,1,4,1,1,1,1,1,1,1,1,6,1,1,1,1,1,1,6,1,1,1,1,1,1,6,0,0,0},
								  {1,3,1,3,1,2,1,1,1,2,1,2,2,9,2,1,1,1,1,1,1,1,1,1,1,1,1,1,2,2,2,1,1,9}};  //储存歌曲1、2音符节拍数

const char LEDtab[16]={0xC0, 0xF9, 0xA4, 0xB0, 0x99, 0x92,
						0x82, 0xF8,0x80, 0x90, 0x88, 0x83, 0xC6, 0xA1, 0x86, 0x8E};//数码管显示的数字编码
						 

unsigned char BUZZ_FLAG = 0;		//播放音乐标志

//接收到的单个字符
unsigned char buffer;

//蓝牙异步串口通信初始化
void USCIA0_init(void);

// 时钟源初始化
void CLK_init(void);

//TA0初始化，并开始计数
void init_TA0(void);

//WDT初始化，并开始计数
void init_WDT(void);

//TA0停止计数
void stop_TA0(void);

//停止WDT的计数
void stop_WDT(void);

//灯、蜂鸣器初始化
void Buzz_init(void);

//数码管初始化
void Tube_init(void);

//数码管显示接收的蓝牙消息，num表示要显示的数字
void display(unsigned char num);

//以音阶lv1来播放音符k
void play (unsigned char k,unsigned char lvl);

//延时函数
void delay(unsigned char b);

int main( void )
{

	unsigned char i;
	WDTCTL = WDTPW + WDTHOLD;//关闭看门狗
	
	__disable_interrupt();	//关总中断

	CLK_init();				//时钟源初始化
	Tube_init();			//数码管初始化
	Buzz_init();			//灯、蜂鸣器初始化
	USCIA0_init();    		//蓝牙异步串口通信初始化
	
	__enable_interrupt();	//开总中断
	LPM0;					//进入低功耗
	while(1)
	{
		while (BUZZ_FLAG==3 || BUZZ_FLAG==9) {
			for (i=0; i<34; i++) {
				if (BUZZ_FLAG==3){			//播放音乐1
					play(music[0][i],lvl[0][i]);
					delay(beat[0][i]);
				}
				else if (BUZZ_FLAG == 9){		//播放音乐2
					play(music[1][i],lvl[1][i]);
					delay(beat[1][i]);
				}
				else break;
			}
		}
		
		LPM0;				//进入低功耗
	}

	return 0;
}

//蓝牙接收数据中断
#pragma vector=USCIAB0RX_VECTOR
__interrupt void UCA0RX_isr(void) {
	IE2 &=~UCA0RXIE;		//关闭接收中断
	buffer = UCA0RXBUF;		//接收一个字符并保存
	display(buffer);		//数码管显示接收的蓝牙消息
		
	switch (buffer) 		//根据接收的不同消息进行不同操作
	{
	case 1:          	//亮灯
		P1OUT &= ~(BIT5+BIT6);
		init_TA0();		//开始计时10s，10s后自动灭灯
		UCA0TXBUF = 1;	//回传成功亮灯信号
		break;
	case 2:          	//灭灯
		stop_TA0();		//停止计时
		P1OUT |= (BIT5+BIT6);
		UCA0TXBUF = 2;	//回传成功灭灯信号
		break;
	case 3:				//歌曲1
		BUZZ_FLAG = 3;	//歌曲1播放
		init_WDT();		//计时20s，20s后自动关音乐
		UCA0TXBUF = 3;	//回传成功播放歌曲1信号
		LPM0_EXIT;		//退出低功耗
		break;
	case 9:				//歌曲2
		BUZZ_FLAG = 9;	//歌曲2播放
		init_WDT();		//计时20s，20s后自动关音乐
		UCA0TXBUF = 9;	//回传成功播放歌曲2信号
		LPM0_EXIT;		//退出低功耗
		break;
	case 4:				//关闭歌曲
		stop_WDT();
		play(0,1);		//关声
		BUZZ_FLAG = 4;	//歌曲播放禁止
		UCA0TXBUF = 4;	//回传成功关声信号
		LPM0_EXIT;		//退出低功耗
		break;
	default:
		break;
	}
	IE2 |= UCA0RXIE;	//打开接收中断
}

//TimerA0 CCR0中断
#pragma vector=TIMER0_A0_VECTOR
__interrupt void timer0_A0_isr(void) {
	stop_TA0();				//停止TA0的计数
	P1OUT |= (BIT5+BIT6);	//灭灯
	UCA0TXBUF = 2;			//回传成功灭灯信号
	TA0CCTL0 &= ~CCIFG;		//中断标志清零
}

//WDT中断
#pragma vector=WDT_VECTOR
__interrupt void WDT_isr(void) {
	stop_WDT();				//停止WDT的计数
	BUZZ_FLAG = 4;			//歌曲播放禁止
	play(0,1);				//关声
	UCA0TXBUF = 4;			//回传成功关声信号
	IFG1 &= ~BIT0;			//中断标志清零
}

// 时钟源初始化
void CLK_init(void) {
	//设置DCO的频率，SMCLK的时钟源默认为DCO
    BCSCTL1=CALBC1_12MHZ;
    DCOCTL=CALDCO_12MHZ; 
	
	BCSCTL3 |= LFXT1S_2;    //ACLK选择12kHZ的VLOCLK
	BCSCTL1 |= DIVA_3;		//ACLK 8分频，即1500Hz
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
	//清TA0R=0, ACLK为计数时钟，增计数方式，1分频，即频率为3000Hz
	TA0CTL |= TACLR;
    TA0CTL |= TASSEL_1 + MC_1 + ID_0;
	TA0CCR0=15000;				//置TA0CCR0值，10秒
	TA0CCTL0 |= CCIE;			//允许CCR0中断
}

//WDT初始化，并开始计数
void init_WDT(void) {
	//计数清零、中断计时模式、时钟源为ACLK、计数到32768发起中断，即20s
	WDTCTL = (WDTPW + WDTCNTCL + WDTTMSEL + WDTSSEL);
	IE1 |= BIT0;	//WDT分中断允许
}

//停止TA0的计数
void stop_TA0(void){
	//清TA0R=0, 不计数，关闭CCR0中断
	TA0CCTL0 &= ~CCIE;
	TA0CTL &= ~(MC0+MC1);
	TA0CTL |= TACLR;
}

//停止WDT的计数
void stop_WDT(void){
	IE1 &= ~BIT0;	//WDT分中断禁止
	//关闭看门狗
	WDTCTL = WDTPW + WDTHOLD;
}


//LED、Buzz初始化
void Buzz_init(void) {
	P1SEL &= ~(BIT5+BIT6);
	P1SEL2 &= ~(BIT5+BIT6);
	P1OUT |= (BIT5+BIT6);
	P1DIR |= (BIT5+BIT6);
	
	//置P2.1输出PWM
	P2SEL |= BIT1;
	P2SEL2 &= ~BIT1;
	P2DIR |= BIT1; 
	TA1CTL = TACLR + TASSEL_2 + MC_0 + ID_2;	//清TAR为0，停止计数，时钟为SMCLK，4分频，即3MHz
	TA1CCTL1 = (OUTMOD_0 + OUT);  //P2.1输出1
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

//以音阶lv1来播放音符k
void play (unsigned char k,unsigned char lvl) {
	switch (k)
	{
	case 0: // 休止符
		TA1CCTL1 = (OUTMOD_0 + OUT);  //P2.1输出1
		TA1CTL = TACLR + TASSEL_2 + MC_0 + ID_2;	//清TAR为0，停止计数，时钟为SMCLK，4分频，即3MHz
		break;
	case 1: //do
		TA1CCR0 = 11364/lvl;
		TA1CCR1 = (unsigned int)(TA1CCR0/100.0*98.0);	//占空比98%
		TA1CCTL1 |= OUTMOD_6;  							//Toggle/set输出模式
		TA1CTL = TACLR + TASSEL_2 + MC_1 +ID_2;	//清TAR为0，增计数，时钟为SMCLK，4分频，即3MHz
		break;
	case 2: //re
		TA1CCR0=10101/lvl;
		TA1CCR1 = (unsigned int)(TA1CCR0/100.0*98.0);	//占空比98%
		TA1CCTL1 |= OUTMOD_6;  							//Toggle/set输出模式
		TA1CTL = TACLR + TASSEL_2 + MC_1 +ID_2;	//清TAR为0，增计数，时钟为SMCLK，4分频，即3MHz
		break;
	case 3: //mi
		TA1CCR0=9091/lvl;
		TA1CCR1 = (unsigned int)(TA1CCR0/100.0*98.0);	//占空比98%
		TA1CCTL1 |= OUTMOD_6;  							//Toggle/set输出模式
		TA1CTL = TACLR + TASSEL_2 + MC_1 +ID_2;	//清TAR为0，增计数，时钟为SMCLK，4分频，即3MHz
		break;
	case 4: //fa
		TA1CCR0=8523/lvl;
		TA1CCR1 = (unsigned int)(TA1CCR0/100.0*98.0);	//占空比98%
		TA1CCTL1 |= OUTMOD_6;  							//Toggle/set输出模式
		TA1CTL = TACLR + TASSEL_2 + MC_1 +ID_2;	//清TAR为0，增计数，时钟为SMCLK，4分频，即3MHz
		break;
	case 5: //so
		TA1CCR0=7576/lvl;
		TA1CCR1 = (unsigned int)(TA1CCR0/100.0*98.0);	//占空比98%
		TA1CCTL1 |= OUTMOD_6;  							//Toggle/set输出模式
		TA1CTL = TACLR + TASSEL_2 + MC_1 +ID_2;	//清TAR为0，增计数，时钟为SMCLK，4分频，即3MHz
		break;
	case 6: //la
		TA1CTL = 6818/lvl;
		TA1CCR1 = (unsigned int)(TA1CCR0/100.0*98.0);	//占空比98%
		TA1CCTL1 |= OUTMOD_6;  							//Toggle/set输出模式
		TA1CTL = TACLR + TASSEL_2 + MC_1 +ID_2;	//清TAR为0，增计数，时钟为SMCLK，4分频，即3MHz
		break;
	case 7: //si
		TA1CCR0=6061/lvl;
		TA1CCR1 = (unsigned int)(TA1CCR0/100.0*98.0);	//占空比98%
		TA1CCTL1 |= OUTMOD_6;  							//Toggle/set输出模式
		TA1CTL = TACLR + TASSEL_2 + MC_1 +ID_2;	//清TAR为0，增计数，时钟为SMCLK，4分频，即3MHz
		break;
	default:
		break;
	}
}

//延时函数
void delay(unsigned char b)
{
	unsigned char j;
	unsigned int i;
	b = b*12;
	for (j=0;j<b;j++)
	{
		for (i=0;i<0xffff;i++){}
	}
}