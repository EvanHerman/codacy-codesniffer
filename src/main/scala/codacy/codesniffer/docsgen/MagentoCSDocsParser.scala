package codacy.codesniffer.docsgen

import better.files.File
import com.codacy.plugins.api.results.{Pattern, Result}

class MagentoCSDocsParser extends DocsParser {

  override val repositoryURL = "https://github.com/magento/marketplace-eqp.git"

  private val sniffRegex = """.*(MEQP1|MEQP2)\/Sniffs\/(.*?)\/(.*?)Sniff.php""".r

  def handleRepo(dir: File): Set[PatternDocs] = {
    (for {
      file <- dir
        .glob(s"$sniffRegex")(File.PathMatcherSyntax.regex)
        .toList
    } yield {
      val sniffRegex(magentoVersion, sniffType, patternName) = dir
        .relativize(file)
        .toString
      handlePattern(dir, file, magentoVersion, sniffType, patternName)
    }).toSet
  }

  private def handlePattern(rootDir: File,
                            sourceFile: File,
                            magentoVersion: String,
                            sniffType: String,
                            patternName: String): PatternDocs = {
    val patternId = Pattern.Id(s"${magentoVersion}_${sniffType}_$patternName")
    val spec = Pattern.Specification(patternId,
                                     findIssueType(sourceFile).getOrElse(Result.Level.Warn),
                                     getCategory(patternId),
                                     parseParameters(sourceFile))

    PatternDocs(spec, description(patternName, patternId), None)
  }

  private def description(patternName: String, patternId: Pattern.Id): Pattern.Description = {
    val title = Pattern.Title(patternName.replaceAll("(\\p{Upper})", " $1").trim)
    Pattern.Description(patternId, title, None, None, None)
  }

}
