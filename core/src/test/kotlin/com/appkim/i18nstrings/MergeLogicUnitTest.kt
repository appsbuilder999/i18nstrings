package com.appkim.i18nstrings

import com.appkim.i18nstrings.translate.GoogleCloudTranslateService
import com.appkim.i18nstrings.translate.MergeLogicImpl
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class MergeLogicUnitTest {

    private val mergeLogic = MergeLogicImpl()
    @Test
    fun replaceDivAndNumbers() {
        val text = "<div 72><div86>Root Explorer<div18<div86>Process Viewer< div18><div86 >记住路径<div18><div86 >Clear<div18><div86>Total:< div18><div 86>Sort<div18><div86>FTP port<div18 ><div86 >Fingerprint<div18><div86>Add Item< div18><div86>仅一次<div1 8><div86 >始终<div18><div86>Wi-Fi P2P<div18> div86>Amaze Document Viewer< div18>< div 86>分享日志<div18><div 86>用Amaze打开<div18><div24><div33>hell, world</div62>"

        val newText = mergeLogic.replaceDivAndNumbers(text)

        val expectText = "<div72><div86>Root Explorer<div18<div86>Process Viewer<div18><div86>记住路径<div18><div86>Clear<div18><div86>Total:<div18><div86>Sort<div18><div86>FTP port<div18><div86>Fingerprint<div18><div86>Add Item<div18><div86>仅一次<div18><div86>始终<div18><div86>Wi-Fi P2P<div18> div86>Amaze Document Viewer<div18><div86>分享日志<div18><div86>用Amaze打开<div18><div24><div33>hell, world</div62>"

        assert(newText == expectText)
    }

    /**
     *
     * Standard： <div18><div86>
     * <div1><div86>
     * <div18S><div86>
     * <div18><. div86>
     * <div18div86>
     * Serbishtdiv18><div86>Ukrainisht
     *
     */
    @Test
    fun splitText() {

        val text = "Amaze File Manager<div18><div86>Hap sirtarin e navigimit<div18><div86>Mbyll sirtarin e navigimit<div18><div86>ikona<div18><div86>Menaxheri i aplikacioneve<div18><div86>Serveri FTP<div18><div86>Cilësimet<div18><div86>Zgjidhni artikullin<div18><div86>Shtypni sërish për të dalë<div18><div86>Hap<div18><div86>Rezervimi<div18><div86>Çinstalo<div18div86>Play Store<div18><div86>Properties<div18><div86>Kopjimi i %d skedarëve APK në %s<div18><div86>Fshi faqerojtësin<div18><div86>Shto te faqeshënuesit<div18><div86>Hyni rrugën<div18><div86>ANULO<div18><div86>KRIJO<div18><div86>Po ngarkohet…<div18><div86>Skedari është bosh<div18><div86>Gabim<div18><div86>Rezultatet e kërkimit të %s<div18><div86>Riemërto<div18><div86>Ruaj<div18><div86>Faqeshënuesit e shtuar<div18><div86>Skedari me të njëjtin emër ekziston tashmë<div18><div86>Emri<div18><div86>Data<div18><div86>Vendndodhja<div18><div86>Madhësia<div18><div86>madhësia e dosjes<div18><div86>Fut Emrin Zip<div18><div86>Nuk u gjet asnjë aplikacion për të hapur këtë skedar<div18><div86>Po kopjon<div18><div86>Lëvizja<div18><div86>Skedari<div18><div86>Dosje<div18><div86>Dosje e re<div18><div86>Fut emrin<div18><div86>Nxjerrja<div18S><div86><div18><div86>Hap Drejtorinë e prindërve<div18><div86>Hap me<div18><div86>Ndaj<div18><div86>Rreth<div18><div86>Ekstrakt<div18><div86>Compress<div18><div86>Po<div18><div86>Jo<div18><div86>Direktoritë<div18><div86>Skedarët<div18><div86>Fshirja e artikujve më poshtë do t'i heqë përgjithmonë nga pajisja juaj dhe nuk mund të rikuperohen.<div18><. div86>Të fshish artikujt përgjithmonë?<div18><div86>Po fshin<div18><div86>Dëshiron të caktosh shtegun aktual si bazë për këtë skedë?<div18><div86>Modaliteti i renditjes së drejtorisë<div18><div86>Rendit sipas<div18><div86>Vetëm kjo dosje<div18><div86>Tema<div18><div86>Random Skin<div18><div86>Cakton ngjyrën kryesore të rastësishme në fillim<div18><div86>Ikonat e ngjyrosjes<div18><div86>Sets ngjyra e ikonës statike. Nuk anashkalon preferencën e ngjyrës së ikonës së drejtorisë.<div18><div86>Home<div18><div86>Ngjit<div18><div86>Historia<div18><div86>Kopjo<div18><div86>Explorer rrënjësor<div18><div86>Vetëm për pajisje me rrënjë. Kontrollojeni vetëm nëse jeni i sigurt për të.<div18><div86>Qasja në rrënjë nuk është dhënë<div18><div86>Zgjidh të gjitha<div18><div86>Fshi<div18><div86>Cakto si bazë<div18><div86>Operacioni nuk mbështetet<div18><div86>Drita materiale<div18><div86>Material Dark<div18><div86>Dita<div18><div86>E zezë (për OLED)<div18><div86>Sistemi (ndiq temën e sistemit)<div18><div86>Dosjet në krye<div18><div86>Skedarët në krye<div18><div86>Asnjë në krye<div18><div86>Emri<div18><div86>Ndryshimi i fundit<div18><div86>Madhësia<div18><div86>Zgjidh një skedar<div18><div86>Shikuesi i procesit<div18><div86>Hapësirë \u200B\u200Be pamjaftueshme<div18><div86>Nuk ka skedar të mbishkruar<div18><div86>Kapërce<div18><div86>Zivendos<div18><div86>Nuk lejohet<div18><div86>Kërkimi %s<div18><div86>U krye<div18><div86>Cakto<div18><div86>Aktivizo modalitetin e rrënjës<div18><div86>Kthehu prapa<div18><div86>Skedar i ri<div18><div86>Nuk ka skedar<div18><div86>hapësirë \u200B\u200Be përdorur<div18><div86>përdorur nga të tjerët<div18><div86>hapësirë \u200B\u200Be lirë<div18><div86>Pamja e listës<div18><div86>Rrjeti Shiko<div18><div86>Lejet<div18><div86>Fshih<div18><div86>Dil<div18><div86>Bëj për të gjithë artikujt<div18><div86>Numri i kolonave në pamjen e rrjetës<div18><div86>Ndryshimet do të ndodhin pasi të rinisni aplikacionin<div18><div86>Authors<div18><div86>Changelog<div18><div86>OK<div18><div86>Ndërfaqja<div18><div86>Të përgjithshme<div18><div86>Shfaq fotografitë e aplikacioneve dhe imazheve<div18><div86>Shfaq fotografitë<div18><div86>Shfaq skedarët dhe dosjet e fshehura<div18><div86>Shfaq datën dhe kohën e modifikimit të fundit<div18><div86>Shfaq datën e modifikimit të fundit dhe Koha<div18><div86>Shfaq madhësinë e skedarëve dhe numrin e artikujve në dosje<div18><div86>Shfaq madhësitë<div18><div86>Të ndryshme<div18><div86>Shfaq lejet e skedarëve dhe dosjeve<div18><div86>Shfaq lejet<div18><div86>Rreth menaxherit të skedarëve Amaze<div18><div86>Versioni<div18><div86>Vlerëso aplikacionin<div18><div86>Dërgo komente<div18><div86>Skedarët e fshehur<div18><div86>Shkruani<div18><div86>Root<div18><div86>Shto shkurtore<div18><div86>Ndryshime të paruajtura<div18><div86>Ke ndryshime të paruajtura, a dëshiron t'i ruash para se të dalësh?<div18><div86>Po ruhet<div18><div86>Instaluesi i paketës<div18><div86>Shiko\n"
        val result = mergeLogic.splitText(text, 140)

        assert(result.size == 140)

    }

    @Test
    fun splitTextForSepecialCase() {
        assert(mergeLogic.splitText("Video<div1div86>Bluetooth", 2).size == 2)
        assert(mergeLogic.splitText("Intellihide Toolbar<div18div86>SHA-256", 2).size == 2)
        assert(mergeLogic.splitText("Ikona dhe banderolat<div18div86>Motori i temave", 2).size == 2)
        assert(mergeLogic.splitText("Serbisht<div18T><div86>div18><div86>Ukrainisht", 3).size == 3)
        assert(mergeLogic.splitText("STOP div18><div86>Artist", 2).size == 2)
        assert(mergeLogic.splitText("Audio<div18div86>Video", 2).size == 2)
        assert(mergeLogic.splitText("Shkruar<di ><div86>nga", 2).size == 2)
        assert(mergeLogic.splitText("Importo<div18><di >Wi-Fi P2P", 2).size == 2)
        assert(mergeLogic.splitText("SHA-256<div18><rectory<6> div18><div86>byte", 3).size == 3)
        assert(mergeLogic.splitText("Njoftimet e Amaze><div1<div86>Amaze FTP Server", 2).size == 2)
        assert(mergeLogic.splitText("Dekriptoj<div18di> Shiriti anësor", 1).size == 1)
        assert(mergeLogic.splitText("Directory<div18by><te<div86>><div86>Njoftimet Amaze", 1).size == 1)
        assert(mergeLogic.splitText("Artist", 1).size == 1)
        assert(mergeLogic.splitText("Format<div18>Owner", 2).size == 2)
        assert(mergeLogic.splitText("Dropbox", 1).size == 1)
        assert(mergeLogic.splitText("Krijo faqeshënues<div18><div86v18>Ndrysho faqeshënuesin<div86>Të fshihet faqerojtësi?", 1).size == 1)
        assert(mergeLogic.splitText("Qasjet e shpejta", 1).size == 1)
        assert(mergeLogic.splitText("Div18><div86>Faqeshënuesit e shiritit anësor", 1).size == 1)
        assert(mergeLogic.splitText("Shfaq faqeshënuesit", 1).size == 1)
        assert(mergeLogic.splitText("shënuesit<div86>Krijo faqeshënues", 2).size == 2)
        assert(mergeLogic.splitText("Lidhja e re në renë kompjuterike<div18D><ropdiv8>", 1).size == 1)
        assert(mergeLogic.splitText("Koha e kaluar><di<div86>Ju lutemi prisni", 1).size == 1)
        assert(mergeLogic.splitText("Dekriptohet<div18><div8Operacionidështoipërskedarëtemëposhtëm", 1).size == 1)
        assert(mergeLogic.splitText("Div18><div86>Faqeshënuesit e shiritit anësor", 2).size == 2)
        assert(mergeLogic.splitText("Bëje<di ><div86>Ke nevojë për ndihmë?", 2).size == 2)
        assert(mergeLogic.splitText("Anonime", 1).size == 1)
        assert(mergeLogic.splitText("v18>Blue ><div86>Dosjet", 1).size == 1)
        assert(mergeLogic.splitText("%1\$d dosje dhe %2\$d skedar", 1).size == 1)
    }

    @Test
    fun removeExtraText() {
        val text = "<div72><div86>Verstek<div86>Gee toestemming<div18><86>Gee toestemming2<div18><div4><div33>hel, wêreld</div62>"
        val result = mergeLogic.removeExtraText(text)
        val expect = "<div72><div86>Verstek<div86>Gee toestemming<div18><86>Gee toestemming2<div18><div24>"

        assert(result == expect)
    }
}